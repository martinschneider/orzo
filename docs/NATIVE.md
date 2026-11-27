# Native backends: x86-64 and AArch64

Plan for compiling Orzo programs to native code besides JVM class files, with an emitter of our own
(Orzo generates the assembly; no LLVM, C or other compiler in between). Nothing of this is
implemented yet.

## Goals and non-goals

Goals:

- `orzo -target <target> Foo.java -o foo` produces a native executable that behaves like
  `java Foo`, for the targets
  - `x86_64-linux` (System V ABI, ELF)
  - `aarch64-linux` (AAPCS64, ELF)
  - later `aarch64-macos` (Apple's AAPCS64 variant, Mach-O)
- The same front end (lexer, parser, desugarings, semantic checks) for all targets; the JVM backend
  keeps working unchanged until it is moved onto the shared middle end.
- Orzo's own syntax (`repeat`, `unless`, `**`, `√`, `⌊⌋`, `÷`, parallel assignment) works natively
  from the first milestone.
- Correctness first, then speed: a naive code generator (every value in a stack slot) comes before
  register allocation and optimizations.

Non-goals (for now):

- The whole JDK. Natively, only a small runtime library is available (see "Runtime library").
- Threads (`synchronized` compiles to nothing), reflection, class loading, `java.io` beyond
  standard output.
- Compiling Orzo itself natively (it needs collections, reflection and file I/O; much later).
- Our own assembler and linker: the first versions write assembly text and call the system's
  `cc` (which runs `as` and `ld`). An ELF writer of our own is optional and comes last.

## Architecture

```
source ─▶ lexer ─▶ parser (+ desugarings) ─▶ attribution ─▶ typed tree
                                                              │
                         ┌────────────────────────────────────┤
                         ▼                                    ▼
                  JVM backend (today's            lowering ─▶ IR ─▶ IR passes
                  CodeGenerator)                                     │
                                                                     ▼
                                              instruction selection (per target) ─▶ MIR
                                                                     │
                                       register allocation, frame layout, prologues
                                                                     │
                                                 assembly printer ─▶ .s ─▶ cc ─▶ executable
                                                                          (+ runtime)
```

### 1. Attribution (shared with the JVM backend)

Today, types are decided while bytecode is generated (`NumExprTypeDecider`, `ExpressionTypes`,
reflection in `MethodCallGenerator`). A native backend can't reuse that, so first:

- A symbol table of the classes, fields, methods and local variables (REVIEW.md, recommendation
  3), for the classes being compiled and for the library classes they use.
- An attribution pass that stores on the tree: the type of every expression, the declaration a
  name refers to, the method (overload) a call invokes, implicit conversions (widening, boxing,
  unboxing, String conversion) and constant values (REVIEW.md, recommendation 1).
- Where library classes come from depends on the target: by reflection on the JDK for the JVM
  (as today), from the runtime library's sources for native targets.

This also is step 1 of the jtreg plan (generics, overload resolution), so it pays off twice.

### 2. Desugarings in the front end

Some desugarings happen in the JVM code generator today and have to move to the front end (or to
the lowering) so that every backend gets them:

- lambdas and method references (synthetic classes, `ExpressionGenerator.generateLambda()`),
- anonymous and local classes capturing variables, inner classes (`this$0`),
- enum methods (`values()`, `valueOf()`), string `switch`, string concatenation,
- the parser's desugarings (`repeat`, `unless`, `assert`, switch expressions, records,
  `synchronized`) stay as they are.

### 3. IR

A small, typed three-address IR (package `io.github.martinschneider.orzo.ir`):

- `Module` (the classes with their layouts, the functions, string constants), `Function`,
  `BasicBlock`, `Instruction`, virtual registers.
- Types: `i8`, `i16`, `i32`, `i64` (signed and `char` as unsigned 16 bits), `f32`, `f64`, `ref`.
- Instructions: constants, moves, arithmetic and comparisons (with Java semantics: `idiv` by zero
  throws, `d2i` saturates), conversions, branches and `switch`, calls (static, virtual through the
  vtable, interface, runtime functions), field and array loads and stores, `arraylength`, `new`,
  `newarray`, `instanceof`, `checkcast`, `return`, `throw`.
- Exceptions: explicit. After a call or check that may throw, a branch on the pending exception
  goes to the handler (or returns to the caller), see "Exceptions".
- Passes: verification, dead code removal, later constant folding, copy propagation, inlining of
  small methods.
- Tests: an IR printer and parser, so that passes and backends can be tested from IR text.

### 4. Native backend (shared part)

Package `io.github.martinschneider.orzo.backend.nativ` (`native` is a keyword):

- `Target`: register sets, calling convention, stack alignment, data layout, assembler syntax.
- MIR: machine instructions with virtual or physical registers, produced by instruction selection
  (macro expansion of IR instructions at first, tree matching later).
- Register allocation: phase 1 assigns every virtual register a stack slot (always correct); phase
  2 linear scan with liveness intervals, callee-saved registers and spill code.
- Frame layout: spill slots, outgoing arguments, saved registers, the alignment the ABI demands
  (16 bytes on both targets).
- Assembly printer per target (GNU `as` syntax), with `.section`s for code, read-only data
  (string constants, vtables, class metadata) and data (static fields).
- Driver: writes the `.s` files, runs `cc` with the runtime library and reports linker errors.

### 5. x86-64 (`backend.nativ.x86_64`)

- System V calling convention: integer arguments in `rdi, rsi, rdx, rcx, r8, r9`, floating point
  in `xmm0–7`, return in `rax`/`xmm0`, callee-saved `rbx, rbp, r12–r15`.
- Scalar SSE2 for `float` and `double`; Java semantics need fix-ups: `cvttsd2si` returns
  `0x80000000` for NaN and out-of-range values (Java: 0 and saturation), `idiv` traps for
  `Integer.MIN_VALUE / -1` (Java: `MIN_VALUE`), comparisons with NaN (`ucomisd` and the parity
  flag).
- Shifts mask their count like Java (`shl` does for 32 and 64 bits).

### 6. AArch64 (`backend.nativ.aarch64`)

- AAPCS64: arguments in `x0–x7` and `v0–v7`, return in `x0`/`v0`, callee-saved `x19–x28`, frame
  pointer `x29`, link register `x30`.
- `fcvtzs` already saturates and returns 0 for NaN; `sdiv` doesn't trap (but division by zero
  returns 0: an explicit check throws `ArithmeticException`).
- Large constants with `movz`/`movk`, addresses with `adrp` + `add :lo12:`.
- macOS later: symbol names with a leading `_`, `@PAGE`/`@PAGEOFF`, Mach-O sections, `x18`
  reserved.

### 7. Object model

- Object: a header (pointer to the class's metadata, which starts with the vtable) followed by
  the fields (sorted by size for alignment, the superclass's fields first).
- Class metadata: name, superclass, interfaces, instance size, the layout of reference fields (for
  the GC), vtable, interface tables, static initializer state.
- Virtual calls: a fixed vtable slot per method (the slots of the superclass first). Interface
  calls: a per-class table searched by interface id (linear at first; selector-indexed tables
  later).
- `instanceof`/`checkcast`: walk the superclass chain, search the interface list (class ids and
  display tables later).
- Arrays: header, `length`, elements; one metadata record per array type (for `instanceof` and
  `getClass()`).
- `String`: an object with a `char[]` (UTF-16, like Java: `charAt()` and `char` stay simple);
  literals are preconstructed objects in read-only data.
- Static fields: in a data section, static initializers run on first use (a guard at each static
  access and call, like the JVM's class initialization).

### 8. Exceptions

- Phase 1: a "pending exception" slot (one global; there are no threads). A throwing operation sets
  it and returns; after every call that may throw, the caller checks it and branches to the
  matching handler (by `instanceof` on the exception's class) or returns itself. `finally` uses
  the copies the parser and code generator already make. Simple, the same on all targets, and
  easy to debug.
- Runtime checks throw the same exceptions as the JVM: `NullPointerException` (explicit null checks
  at first), `ArrayIndexOutOfBoundsException`, `ArithmeticException`, `ClassCastException`,
  `NegativeArraySizeException`.
- An uncaught exception prints `Exception in thread "main" <class>: <message>` and exits with 1.
- Later: zero-cost unwinding with tables (`.eh_frame`/DWARF CFI), which is much more work.

### 9. Memory management

- Phase 1: a bump allocator without collection (most test programs allocate little).
- Phase 2: a mark-sweep collector: conservative scanning of the stack and registers, precise
  scanning of the heap with the classes' reference layouts. No stack maps needed.
- Later: precise stack maps (the backend knows which slots hold references), a copying or
  generational collector.

### 10. Runtime library

- A small layer in assembly per target: process entry (`_start` or `main`), `write` and `exit`
  system calls (or libc's), memory from `mmap`. Using libc at first is fine (`cc` links it anyway).
- `java.lang` written in Orzo and compiled by the native backend: `Object`, `String`,
  `StringBuilder`, `Integer`/`Long`/`Double`/... (boxing, `parseInt`, `toString`), `Math`,
  `System.out` (`println` overloads), `Throwable` and the common exceptions, `Character`,
  `Comparable`, `Iterable`/`Iterator`.
- Later: `java.util` (`ArrayList`, `HashMap`, `HashSet`, `Arrays`, `Collections`), functional
  interfaces (`java.util.function`), `Objects`.
- Number formatting and parsing must match Java exactly (`Double.toString()` is the hardest part:
  shortest representation, Ryu or the JDK's algorithm).
- For a native target, the attribution resolves library classes against these sources instead of
  reflection: calling an API the runtime doesn't have is a compile error.

### 11. Testing

- Unit tests for the IR (printer/parser, verifier, passes) and instruction selection (IR text in,
  assembly text out).
- End-to-end: the programs in `src/test/resources/.../tests` and `examples` with their `.output`
  files, compiled for each native target and run; a list of the programs each target supports
  that only grows (like the self-compilation skiplist did).
- AArch64 on x86-64 machines with `qemu-aarch64` (user mode) and a cross toolchain
  (`aarch64-linux-gnu-gcc`); the tests are skipped when the tools aren't installed.
- Differential: the output of the native program must equal the JVM's.

## Milestones

Each milestone ends with its programs running on both Linux targets (from M1).

- **M0: infrastructure.** `-target` option, attribution and IR for a subset (primitive types,
  static methods, local variables, control flow, `System.out.println` of primitives and string
  literals), naive x86-64 code generation, runtime entry and printing. HelloWorld and Fibonacci run
  on x86-64.
- **M1: AArch64.** The same programs on AArch64 (qemu in the tests); `examples/` with floating point
  (`√`, `**`, `Math`), integer edge cases (division, shifts, overflow), Orzo's own syntax.
- **M2: arrays, strings and registers.** Arrays (also multi-dimensional, which the JVM backend
  doesn't support yet), `String` and concatenation, `StringBuilder`, `char`; linear scan register
  allocation.
- **M3: objects.** Classes, fields, constructors, inheritance, virtual and interface calls,
  `instanceof` and casts, static fields and initializers, enums, `Object` methods.
- **M4: exceptions.** `throw`, `try`/`catch`/`finally`, the runtime checks, uncaught exceptions.
- **M5: the rest of the language.** Nested, inner, local and anonymous classes, lambdas and method
  references, boxing, generics (erased like on the JVM), switch on strings and enums, records,
  collections in the runtime library. Most of `tests/` runs natively.
- **M6: memory and platforms.** The garbage collector, `aarch64-macos`.
- **Later:** optimizations (constant folding, inlining, peephole), zero-cost exceptions, precise GC,
  our own ELF writer, compiling Orzo itself natively.

## Open questions

- Does the JVM backend move onto the IR as well (one code generator front, two back ends), or does
  it keep generating from the typed tree? (Its output must stay as small as javac's.)
- Runtime library in Orzo only, or C for the parts that are hard to write without pointers
  (allocator, GC, system calls)?
- `String`: UTF-16 like Java (simple, compatible) or Latin-1/UTF-16 like the JDK's compact
  strings (smaller)?
- How strict to be about Java semantics that cost speed natively (null checks on every
  dereference, class initialization guards)?

