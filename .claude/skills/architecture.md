# Orzo Architecture

Orzo is a Java-to-JVM-bytecode compiler written in Java, targeting class file version 50 (Java 6 semantics).
It can compile a subset of its own source — the self-compilation profile tests this.

## Pipeline

```
Source files → Lexer → TokenList → Parser → AST (Clazz[]) → CodeGenerator → .class files
```

Entry point: `Orzo.java` — parses CLI args, invokes each stage.

## Lexer (`lexer/`)

- `Lexer` — converts source chars into `TokenList`
- `TokenList` — cursor-based list; parsers call `peek()` / `consume()` / `rewind()`
- Token types: `Identifier`, `Keyword`, `Type`, `Num`, `IntLiteral`, `FPLiteral`, `BoolLiteral`,
  `Str`, `Chr`, `Operator`, `Sym`, `Scope`, `TernaryExpression`, `EOF`
- `Keywords` / `Operators` / `Symbols` / `Scopes` — string-constant registries

## Parser (`parser/`)

- Each construct has its own `*Parser` class; all return `null` on no-match (not exceptions)
- `StatementParser` — top-level dispatcher; tries each statement parser in turn
- `ClassParser` — parses class/interface/enum declarations; populates `importMap` in `ParserContext`
- `ParserContext` — shared state: current class, import map, errors
- Productions (`parser/productions/`) — plain data classes: `Clazz`, `Method`, `Declaration`,
  `Expression`, `Assignment`, `IfStatement`, `WhileStatement`, `ForStatement`, `DoStatement`,
  `ConstructorCall`, `MethodCall`, `ArrayInit`, `ArraySelector`, `Argument`, `Import`, etc.
- `ExpressionParser` — handles full expression grammar including ternary, casts, method calls,
  field access chains; returns `Expression` containing a list of tokens
- `ConstructorCallParser` — skips generic type args after `new TypeName<...>(`

## Code Generator (`codegen/`)

### Top level

- `CodeGenerator` — orchestrates: constant pool → fields → methods → writes `.class` file
  - Class file version 49 (not 50): Java 21 removed type-inference verifier for v50,
    which would require StackMapTable. v49 uses the old inferencing verifier.
  - `fields()` skips inherited fields — writing them creates shadow fields that hide parent fields
- `CGContext` — per-class mutable state: output stream, constant pool, identifier map, errors
- `ConstantPoolProcessor` — builds the constant pool; must include the base class even if unused
- `FieldProcessor` — processes field declarations; `getInstanceFieldMap()` does recursive parent
  traversal to find inherited fields
- `MethodProcessor` — registers methods; only static methods are registered under their
  unqualified name to avoid incorrect resolution of instance methods
- `MemberProcessor` — `processFields()` adds inherited fields to the class's constant pool and
  fieldMap using `putInheritedField()` (distinct from own fields)

### Identifier tracking

- `IdentifierMap` — maps names → `VariableInfo` or `FieldInfo`; `putInheritedField()` tags
  inherited fields so they are not written to the class file as own fields
- `VariableInfo` — local variable: slot index, type
- `FieldInfo` — class field: owner class name, descriptor

### Generators (`codegen/generators/`)

- `StatementDelegator` — dispatches statements to the right generator
- `ExpressionGenerator` — core expression evaluator; handles identifiers, operators, method calls,
  field access, casts, boolean branch emission
  - `handleId()`: skips `convert1()` for reference-to-reference type changes
  - Boolean branch logic: `NEGATE + reverseComp=true` → emit `IFNE`; after token loop if
    `reverseComp=true` and no branch emitted and type=BOOLEAN → emit `IFEQ`
  - `prevLoadedRef` tracks the last loaded object ref so chained field access (`other.field`)
    emits the correct `GETFIELD`
- `AssignmentGenerator` — handles `=` and compound assignments; `super` treated same as `this`
  when loading the receiver for field stores; tracks `thisLoaded` flag to avoid double-push
- `MethodCallGenerator` — resolves and invokes methods; `getClass()` and `equals()` fall back to
  `Object` method signatures; instance methods use `invokevirtual`, not `invokestatic`
- `IncrementGenerator` — `IINC` only works on locals; field increment/decrement uses explicit
  load/add/store with the correct objectref
- `DeclarationGenerator` — uninitialised array declarations emit `aconst_null + astore`
- `BasicGenerator` — handles casts; skips boolean-to-boolean cast; emits `checkcast` for
  reference type casts; entry point for compiling a single statement
- `StoreGenerator` — must handle user-defined class types in the default case
- `InvokeGenerator` — emits invoke bytecodes; instance calls push `this` first
- `OperatorMaps` — maps operator symbols to bytecodes; reference-type `==`/`!=` fall back to
  `IF_ACMPEQ` / `IF_ACMPNE` when types are not in the operator map

### Bytecode helpers

- `OpCodes` — bytecode constant definitions (includes `ACONST_NULL`, `IF_ACMPEQ`, `IF_ACMPNE`)
- `DynamicByteArray` — growable byte buffer for emitting bytecode
- `ByteUtils` / `ByteUtils2` — helpers for encoding ints/shorts/longs into bytes
- `ConstantPool` — runtime pool; entries are `Constant` subclasses
- `NumExprTypeDecider` — decides expression type for numeric operations

## Self-compilation

- `allowlist.txt` — files orzo can currently compile of itself
- `selfcompile.sh` — strips comments, compiles allowlisted files 3× with orzo, measures size vs javac
- `find_loop.sh` — binary-search harness to isolate infinite loops in self-compilation
- `progress` file — single float (LOC%) written by `selfcompile.sh`; read by Maven for the badge

