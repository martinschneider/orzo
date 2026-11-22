# Orzo design and architecture review

State as of November 2026: Orzo compiles all of its own sources (about 21,600 lines in 170 files), and
its three bootstrap passes produce class files of identical total size. Its output is 4.4x the size of javac's, and it
compiles about 5x slower than javac (see the metrics in README.md). This review describes how the
compiler is built, where the design causes recurring bugs, and what to change, roughly ordered by impact.
Concrete open issues are tracked in [TODO.md](TODO.md).

## How it works today

```
source ─► Lexer ─► TokenList ─► recursive-descent ProdParsers ─► Clazz/Method/Statement "productions"
                                 (expressions: ExpressionParser ─► tree ─► PostfixLowering ─► postfix lists of tokens)
        ─► CodeGenerator (per class, CGContext) ─► statement generators ─► ExpressionGenerator
        ─► raw bytecode in DynamicByteArray ─► StackMapTableBuilder (re-simulates the bytecode)
        ─► ConstantPool + class file
```

- **Lexer**: a hand-written scanner producing `Token` subclasses (`Identifier`, `IntLiteral`, `Operator`, ...).
- **Parser**: one `ProdParser` per construct, with backtracking via `tokens.setIdx(...)`. The
  `StatementParser` tries the sub-parsers in a fixed order. `ExpressionParser` parses an expression
  into an immutable tree (`parser.tree`) by precedence climbing, without modifying the tokens.
  `PostfixLowering` turns the tree into the postfix token lists the code generator works with: dotted
  names and calls become chains of `Identifier`/`MethodCall` tokens linked via `next`.
- **Types**: there is no separate semantic analysis. `NumExprTypeDecider` guesses an expression's type
  for overload resolution. `ExpressionGenerator` tracks types again while emitting code: the requested
  context `type`, an `exprTypeStack`, and `ctx.opStack`.
- **Symbols**: string-keyed maps (`methodMap` with keys like `"Foo.bar(I)V"`, `staticFieldMap`, a flat
  name-to-slot `IdentifierMap`), built per class from the classes in the compilation run plus reflection
  (`Class.forName`) on the compiler's own classpath.
- **Code generation**: one generator per statement type, writing bytes into `DynamicByteArray`s. Branch
  offsets are patched afterwards, and sub-buffers are merged with their exception-table entries rebased.
  `StackMapTableBuilder` (1,800 lines) then reconstructs stack map frames and the maximum stack depth by
  simulating the finished bytecode.

## What works well

- The test suite is large: 270 end-to-end programs and 340 test classes, plus three-pass
  self-compilation as an integration test. It found most of the bugs fixed during the self-compilation
  effort.
- The staged bootstrap (javac-built, then Orzo-built compiler, recompiling itself) is a strong
  correctness check, and the metrics make progress visible.
- The code is small and readable per construct (one parser and one generator per statement type), so it
  is easy to find where a feature lives.
- The Orzo-only syntax (`repeat`, `unless`, `**`, `√`, parallel assignment) stays in the parser and
  generators, so it doesn't spread through the code.

## Recommendations

### 1. Add a semantic analysis (attribution) phase: type every expression once (high)

Most bugs fixed recently had one root cause. Types are inferred twice, in two different ways, and during
code generation an operand is evaluated "in" the type its *context* asks for instead of its own type.
Examples: `char + char` printed a `char`; a cast inside an expression was a no-op; `int` literals were
pushed as `boolean`; `x - aLong` wasn't promoted; a comparison typed as `int` needed
`RetGenerator.convertReturnValue()` to suppress a bogus error; string concatenations passed as `Object`
weren't concatenated.

Introduce an attribution pass between parsing and code generation, like javac's `Attr`. It annotates
every expression node with its static type. It also applies binary numeric promotion, boxing and
unboxing, resolves overloads (JLS 15.12, all three phases), infers generic method type arguments from
argument and target types, and resolves names (local, field, nested or outer class, static import). Code
generation then only reads these annotations and inserts the conversions the attributed tree prescribes.

Migration path: let `NumExprTypeDecider` store its result on each `Expression`. Make
`ExpressionGenerator` use the stored types instead of `type`, `exprTypeStack` and `opStack` guesses,
then delete the guessing code. This also removes the side channels in recommendation 5.

### 2. Replace postfix token lists with an expression tree and a Pratt parser (high, partly done)

Done: `ExpressionParser` builds immutable tree nodes (`LiteralNode`, `NameNode`, `SelectNode`,
`CallNode`, `IndexNode`, `NewNode`, `UnaryNode`, `BinaryNode`, `TernaryNode`, `CastNode`,
`InstanceOfNode`) by precedence climbing. Precedence and associativity come from one table (the
`Operators` precedences; assignments are right-associative). The parser doesn't modify tokens any more, so a failed
parse attempt leaves nothing behind (before, it rewrote `√`/`⌊⌋` in the token list, removed array
selector tokens and linked lexer tokens via `next`; `AssignmentParser` removed tokens and the old
parser depended on those stale links). The assignment, increment, call and constructor statement
parsers use the tree as well, and a cast now applies only to its operand (`(int) d * 1.5` used to
cast the whole product).

Still to do:

- The code generator still consumes postfix token lists, which `PostfixLowering` produces from the
  tree. Every consumer still finds the operands of an operator by scanning backwards
  (`findRightOperandStart`, `withoutCastOperands`, the `**` operand skipping), which is fragile
  (integer `**` once swapped its operands this way). Migrate `NumExprTypeDecider` and
  `ExpressionGenerator` to the tree, then delete the lowering and the token subclasses that only
  exist to encode trees in token lists (`CastOperator`, `CastMethodCall`, `TernaryExpression`,
  `InstanceofToken`, `MethodCall`/`ConstructorCall` as `Identifier`s).
- Lambdas, method references and array creations are still `OpaqueNode`s holding postfix
  expressions.
- Tree nodes have no source positions yet (errors use the position of their statement).

### 3. One symbol table, backed by class files instead of reflection (high)

There are seven separate class-name resolvers: `ClassParser.resolveClassName`,
`TypeUtils.resolveSimpleType`, `ExpressionGenerator.convertToJVMClassName`,
`MethodCallGenerator.resolveClassName`, `BasicGenerator.resolveJvmClassName`,
`ExpressionParser.resolveCastType` and `TypeArgsParser.resolve`. There are also about 50 `Class.forName`
calls. Each resolver implements a different subset of the scoping rules (nested classes, imports,
`java.lang`, same package), so the same name can resolve differently in different places. Reflection
also ties the compiled program to the compiler's own classpath, and there is no `-classpath` option.
Because later bootstrap passes reflect on Orzo-built classes, Orzo has to emit attributes such as
`Signature` and `ACC_VARARGS` exactly right just so the next pass can see them.

Introduce `ClassSymbol`, `MethodSymbol` and `FieldSymbol` with a single `Scope` lookup chain: block,
method, class, enclosing classes, single-type imports, same package, on-demand imports, `java.lang`. Back
it by two sources:

1. The classes of the current compilation run.
2. Class files read from a configurable classpath via the JDK's `java.lang.classfile` API.

Overload resolution, inherited members, interface detection (`TypeUtils.isInterface()`), bridge
generation and generic signatures then all operate on symbols. String keys like `"Foo.bar(I)V"` go
away, and so do the bare-name registration bugs fixed in this round.

### 4. Generate bytecode through a label-based assembler, ideally `java.lang.classfile` (high)

Code generation writes raw bytes, patches branch offsets, merges sub-buffers (rebasing exception tables,
`labeledBreaks`, `pendingBreaks`) and then rebuilds type information by re-simulating the bytecode in
`StackMapTableBuilder`. That class re-implements part of the JVM verifier. It had gaps such as all six
`DUP` forms treated as `DUP` and `POP2` ignoring long/double, and it is the largest source of
`VerifyError`s.

The JDK's ClassFile API (`java.lang.classfile`, final since Java 24) provides symbolic labels and
exception handlers, a constant pool with automatic deduplication, and computes stack map frames and the
maximum stack depth itself. Moving to it would delete `StackMapTableBuilder`, `ConstantPool`,
`ConstantPoolProcessor`, the patching logic in `DynamicByteArray` and most index bookkeeping. That is
roughly a quarter of the code generator. If an external dependency is unwanted for a self-hosting
compiler, the same API ships with the JDK, so Orzo can still compile itself. The alternative is a small
in-house assembler with `Label` objects and frames computed from the attributed types (recommendation 1)
instead of from the bytes.

### 5. Make generators stateless and make data flow explicit (medium)

Generators pass information through mutable fields set before a call and read inside it:
`lastGenericType`, `argsReceiverGeneric`, `argsExpectedGeneric`, `ExpressionGenerator.lambdaTargetGeneric`,
`callTargetGeneric`, `typeFromArrayElement`, `prevLoadedRef`, and the `skipLoad`/`assignTargets` maps.
Statement-level and expression-level calls are handled by different `MethodCallGenerator` instances, so
a field set on one is invisible to the other; this caused the inference bug fixed in this round.
`CGContext` is a god object with 25 public mutable fields, initialized per class.

Once types are attributed (recommendation 1), most of these side channels are unnecessary. For the rest:

- Pass a small immutable `GenContext` (method, scope, expected type) as a parameter.
- Create each generator once.
- Keep per-method state (locals, labels, the exception table) in a `MethodBuilder` object.

### 6. Scoped local variables and a real slot allocator (medium)

`IdentifierMap` is a flat name-to-slot map per method, with no block scopes: two sibling blocks can't
declare the same name with different types. Temporary locals are allocated ad hoc and never reused
(`$switch_`, `$concat_`, `$promote_`, `$pow_`). Slot reuse was disabled because `StackMapTableBuilder`
types each slot by a single variable. With scoped symbols (recommendation 3) and frames computed from
types (recommendation 4), locals can reuse slots when their scope ends, as javac does.

A first step is done: `LocalVariableChecker` walks each method with the block structure and reports a
local variable that redeclares one still in scope (or a parameter), while sibling blocks may reuse a
name.

### 7. Error handling and diagnostics (medium; the basics are done)

**Done** (November 2026): one error list for the whole compilation run (before, only the last input
file's errors were reported), `File.java:line:col: error: message` with tokens shown as written, no
class files and exit code 1 on errors, no code generation after syntax errors, and `CompileErrorTest`
for programs that must not compile. Several silent fallbacks now report errors: incomplete
expressions (`int y = ;`, `1 +;`, `(1 + 2;`), `new Unknown()` (used to call a made-up constructor),
`break`/`continue` outside a loop, `return 5;` in a `void` method, missing return statements, missing
array dimensions, multi-dimensional arrays (used to emit nothing), and the obvious `int`/`String`
mismatches. Earlier, `~x`, text blocks, switch fall-through and `System.out.println()` compiled to
wrong code without an error.

Also done: error recovery (an unparsable statement or class member is reported and skipped, so one
run reports independent errors), `break`/`continue` errors at the statement (enclosing loops, switches
and labeled blocks are tracked during code generation), internal compiler errors (an exception while
parsing a file or generating a class is reported as an error, and the other files and classes are
still checked), and usage errors exit with status 2.

Still to do:

- **Structured diagnostics**: severity (error/warning), a stable code and notes ("did you mean ...?").
  (The report already shows the source line with a `^` under the error.)
- **Positions for errors inside expressions**: code generation errors are reported at the start of
  their statement; a typed expression tree (recommendation 2) can carry the position of each node.
- **Explicit resolution failures**: ~50 `catch (ClassNotFoundException e)` blocks fall back silently;
  an unresolved name should end up as one "cannot find symbol" error (see recommendation 3).
- **Real type checking**: only the clearest incompatible assignments are detected; the attribution phase
  (recommendation 1) is the place for JLS assignment and method invocation conversions.

### 8. Differential testing against javac (medium; the main part is done)

The expected outputs of the end-to-end tests are hand-written, and some encoded Orzo's bugs: `'Z'` for
`'(' + '2'`, `@` for `64 * chr`, swapped `**` operands in the bytecode tests. Add a harness that compiles
every test program with both javac and Orzo, runs both, and compares their output. It should also load
every generated class with full verification. Expected outputs then only need to exist for Orzo-only
syntax. A small corpus of "probe" programs for unsupported features (one per README checklist item)
would keep the README honest automatically.

**Done** (November 2026): `DifferentialTest` compiles every end-to-end test program with javac,
runs it and checks it prints the expected output, so all expected outputs of valid Java programs are
Java's (21 of 256 programs aren't valid Java: Orzo-only syntax, implicit narrowing of `int` literals
to `byte`/`short`, unchecked checked exceptions). Still to do: verify every Orzo-generated class with
full verification, and generate probe programs for the README checklist.

Exact-bytecode unit tests (`ExpressionGeneratorTest` and others) are brittle and test the implementation
rather than its behaviour. Keep them for peephole decisions (e.g. `IFEQ` versus `IF_ICMPEQ` against 0)
and use behaviour tests for the rest.

### 9. Turn the self-compilation fixpoint into an invariant (done)

`selfcompile.sh` fails unless all three passes generate byte-identical class files: the javac-built
compiler (pass 1) and the Orzo-built ones (passes 2 and 3) compile Orzo's sources to exactly the same
bytes, so the self-compiled compiler behaves like the javac-built one and code generation is
deterministic.

### 10. Performance and output size (low, after 1–4)

- **Compile time (5x javac)**: reflection is called repeatedly (`getMethods()`, `Class.forName` inside
  overload resolution) and never cached. The method and field maps are rebuilt per class from all
  classes, which is O(n²). Expressions are re-typed many times (`new NumExprTypeDecider(...)` in 18
  places, often on sub-expressions). A symbol table (recommendation 3) and attribution
  (recommendation 1) fix most of this; profile with JFR first.
- **Class size (4.4x javac)**: likely full stack map frames everywhere, redundant conversions and
  `CHECKCAST`s, no constant folding, and constant pool entries added "just in case". A real assembler
  (recommendation 4) emits compact frames and only the constants that are used. A per-method size
  comparison against javac would show where the bytes go.

### 11. Smaller cleanups

- `System.out.println` is still special-cased with hand-written bytecode
  (`MethodCallGenerator.print()`). Removing it requires recommendation 1, because the generic path
  currently picks wrong overloads for comparisons and booleans; this was tried in this round and
  reverted.
- `OperatorMaps` encodes opcode tables per type name string. With attributed types this becomes a
  function of a `PrimitiveType` enum.
- `Type` (a token class) and type-name strings (`"int"`, `"java.lang.String"`, `"Ljava/lang/String;"`,
  `"[I"`) are used interchangeably. `TypeUtils.jvmDescriptorToTypeName()` is called defensively in many
  places. Use one `TypeRef` value type.
- `ObjectUtils` implements `equals`, `hashCode` and `toString` of all productions via reflection. This is
  slow and hides structure. Records would be the natural replacement once Orzo supports them.
- `FactoryHelper` (test-data builders) lives in production code and is used by the parser; move the
  test-only parts to `src/test`.
- Keep the Orzo-only syntax as pure desugarings in the parser (`repeat` to `for`, `unless` to `if (!…)`,
  `**` to a call, parallel assignment to temporaries), so later phases only see core Java.

## Suggested order

1. Differential testing against javac (8) and errors instead of silent fallbacks (7). These are cheap
   and catch regressions during the bigger refactorings.
2. Expression tree (2), then attribution (1). These remove the largest class of bugs and the side
   channels (5).
3. Symbol table backed by class files (3), which enables `-classpath` and removes reflection.
4. ClassFile-API back end (4), which deletes stack map reconstruction and constant pool bookkeeping and
   enables slot reuse (6).
5. Then performance and size (10).

