# Orzo TODO

Tracked issues and improvement opportunities found during code review.
Priority: **H**igh / **M**edium / **L**ow.

---

## Dead Code / Redundancy

| Priority |         Location         |                                                                      Description                                                                      |
|----------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| M        | `ConstantPool.add()` L90 | "shouldn't this be size-1?" for Long/Double — the index returned for double-slot constants may be off by one. Passes tests but needs closer analysis. |
| L        | `GlobalIdentifierMap`    | Comment says "TODO: this is temporary, merge this into values" — the split between fieldMap/localMap/inheritedFieldNames should be unified.           |

---

## Hardcoded Shortcuts (won't generalise to real-world code)

| Priority |                    Location                     |                                                                                           Description                                                                                           |
|----------|-------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| H        | `MethodProcessor.addJavaLang()`                 | Only reflects static methods from a hardcoded list of classes (Math, Double, Float, Character, Integer, Long, String, ObjectUtils). Should use the import list to reflect any referenced class. |
| H        | `MethodCallGenerator.generate()` L~255          | `System.out.println` is special-cased with hand-written bytecode. Other print/IO methods are unsupported. Generalise via `getstatic` + `invokevirtual` driven by reflection.                    |
| H        | `MethodCallGenerator.generate()` L~73–112       | `toString()`, `hashCode()`, `getClass()`, `equals()` on arbitrary receivers are hard-coded Object fallbacks. Should be resolved via reflection like other instance methods.                     |
| H        | `CodeGenerator.supportPrint()`                  | Unconditionally adds `java/lang/System` and `java/io/PrintStream` to the constant pool even for classes that never print. Should only add when `System.out.println` is actually used.           |
| M        | `BasicGenerator.primitiveToString()`            | Hardcoded `String.valueOf` descriptor per primitive type. Correct but brittle — consider deriving descriptor from reflection.                                                                   |
| M        | `BasicGenerator.autobox()`                      | Hardcoded wrapper class + `valueOf` descriptor per primitive. Same concern as above.                                                                                                            |
| M        | `FieldProcessor.getInstanceFieldMap()`          | Falls back to a hardcoded list of java.lang classes when `Class.forName` fails for a simple name. Should use the caller's import map instead.                                                   |
| L        | `MethodCallGenerator.findMethodViaReflection()` | Normalises `java.lang.String` return type to the `STRING` constant but not other common types. Should be generalised.                                                                           |

---

## Missing Features / Known Gaps

| Priority |           Location            |                                                                                      Description                                                                                      |
|----------|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| H        | `MethodCallParser`            | Cannot parse chained method call *statements* like `a.b(x).c(y)` — only `ExpressionParser` handles chains. Statements that chain calls must be rewritten with intermediate variables. |
| H        | `DeclarationParser`           | Declarations inside `if`/`while`/`for` bodies are not pre-scanned by `MemberProcessor.processLocalVars()`, so they may not get correct local variable slots in some edge cases.       |
| M        | `TypeUtils.descr()`           | Multi-dimensional arrays not supported (marked TODO).                                                                                                                                 |
| M        | `ArrayInitParser`             | Multi-dimensional array initialisation not supported (marked TODO).                                                                                                                   |
| M        | `ForParser`                   | Statement sequences inside for-loop initialisers not supported (marked TODO).                                                                                                         |
| L        | `ConstantPool`                | Long and Double constants take two slots in the pool; the current index accounting may be wrong (see Dead Code section).                                                              |

---

## Architecture / Refactoring

| Priority |               Location               |                                                                                       Description                                                                                        |
|----------|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| M        | `MethodParser` / `DeclarationParser` | Bootstrap `NoSuchFieldError` catch block for `Scopes.accFlag` is a temporary workaround for self-compilation ordering. Should be resolved by proper compile-order dependency resolution. |
| M        | `ExpressionParser.parse()`           | Very long method (~170 lines), marked TODO "simplify and document this". Refactor with a strategy/visitor approach per token type.                                                       |
| M        | `ExpressionGenerator.eval()`         | Same as above — large dispatch method.                                                                                                                                                   |
| M        | `MethodCallGenerator`                | `findMethodViaReflection` and `findMethodViaReflectionTyped` are similar; could be merged with an optional arg-type list parameter.                                                      |
| L        | `Type.java`                          | TODO comment says "replace this with a simple string". The wrapper class adds no value beyond the string it wraps.                                                                       |
| L        | `IdentifierMap`                      | TODO "error handling" for duplicate variable names.                                                                                                                                      |

---

## Testing

| Priority |        Location        |                                             Description                                             |
|----------|------------------------|-----------------------------------------------------------------------------------------------------|
| M        | `DeclarationGenerator` | No unit test for the null-init reference-type fix (aconst_null for `Foo f;` declarations). Add one. |
| M        | `PushGenerator`        | No unit test for `boolean` push (fixes silent stack underflow). Add one.                            |

