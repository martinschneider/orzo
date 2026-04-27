# Codegen Bug Patterns

Recurring bug categories found while extending self-compilation. Check these first when a new
file fails to compile or produces wrong bytecode.

---

## 1. Inherited fields

**Symptom:** `getfield` resolves wrong field, or field stores silently go to a shadow field on
the subclass instead of the parent.

**Root causes / fixes:**
- `CodeGenerator.fields()` must skip inherited fields. Writing them to the class file creates a
shadow field that hides the real parent field at runtime.
- `FieldProcessor.getInstanceFieldMap()` must do recursive parent traversal via `getSuperclass()`
so inherited fields are visible for codegen.
- `MemberProcessor.processFields()` must add inherited fields to the current class's constant pool
and `fieldMap` using `IdentifierMap.putInheritedField()`.
- Use `IdentifierMap.putInheritedField()` (not `put()`) so the inherited/own distinction is
preserved downstream.

---

## 2. `this` / `super` receiver confusion

**Symptom:** Field store on `super.field = value` fails or stores to wrong location.
`ExpressionGenerator` can't resolve `this` or `super` as an identifier.

**Root causes / fixes:**
- `AssignmentGenerator`: treat `super` identically to `this` when loading the receiver for
`putfield` — they produce the same bytecode (load slot 0).
- `ExpressionGenerator`: handle `this` and `super` explicitly as identifiers that load slot 0.
- Track `thisLoaded` flag in `AssignmentGenerator` to avoid double-pushing `this`.

---

## 3. Instance vs. static method dispatch

**Symptom:** Instance method call compiles but throws `AbstractMethodError` or
`NoSuchMethodError` at runtime; or static methods are called with `invokevirtual`.

**Root causes / fixes:**
- `InvokeGenerator` must push `this` (slot 0) before instance method args and use
`invokevirtual`, not `invokestatic`.
- `MethodProcessor` must only register static methods under their unqualified name.
Registering instance methods there causes them to be resolved as static calls.
- `CodeGenerator` must reserve local slot 0 for `this` in all non-static methods, not just
constructors.

---

## 4. Field increment / decrement

**Symptom:** `IINC` emitted for a field, causing `VerifyError` (IINC only valid for locals).

**Fix:** `IncrementGenerator` must detect fields and emit explicit load/add/store:
`getfield` → push 1 → `iadd`/`isub` → `putfield`, with the correct objectref loaded first.

---

## 5. Boolean branch emission

**Symptom:** `if (!boolExpr)` or `if (boolExpr)` with no comparator produces wrong branches or
garbage branch offsets.

**Root causes / fixes (in `ExpressionGenerator.eval()`):**
- `NEGATE + reverseComp=true` → emit `IFNE` (not `ICONST_1 + IXOR`, which breaks branch offsets).
- After the token loop: if `reverseComp=true`, no branch yet emitted, type=BOOLEAN → emit `IFEQ`.

---

## 6. Reference type comparisons

**Symptom:** `==` / `!=` between objects causes a `VerifyError` because integer compare opcodes
(`IF_ICMPEQ` etc.) are used on reference types.

**Fix:** `OperatorMaps` + `ExpressionGenerator` must fall back to `IF_ACMPEQ` / `IF_ACMPNE`
when operand types are not in the numeric operator map.

---

## 7. Type narrowing in long expressions

**Symptom:** Expression like `(val >> 56) & 255` truncates early; the `& 255` sees an `int`
instead of a `long`.

**Fix:** `ExpressionGenerator` must defer narrowing conversion from `long` when more tokens
follow in the expression. Only narrow after the final operator.

---

## 8. Uninitialised array declarations

**Symptom:** `byte[] x;` (no initialiser) causes a compiler error or NPE during codegen.

**Fix:** `DeclarationGenerator` must emit `aconst_null + astore` for uninitialised array
declarations. `OpCodes` must define `ACONST_NULL`.

---

## 9. Class file version / StackMapTable

**Symptom:** Class loads fine under javac but throws `VerifyError` when compiled by orzo, e.g.
`Expecting a stackmap frame at branch target`.

**Root cause:** Class file version 50 (Java 6) requires `StackMapTable` attributes for all
branching bytecode. Java 21 enforces this strictly.

**Fix:** Use version 49 (Java 5). The type-inferencing verifier still works for v49.
See `CodeGenerator.JAVA_CLASS_MAJOR_VERSION = 49`.

**TODO:** Implement `StackMapTable` and upgrade back to v50+.

---

## 10. Reference-to-reference type cast noise

**Symptom:** `checkcast` or `convert1()` emitted between two reference types, causing
`VerifyError`.

**Fix:** `ExpressionGenerator.handleId()` must skip `convert1()` for reference-to-reference
type changes. `BasicGenerator` must skip boolean-to-boolean cast.

---

## 11. Parser consuming wrong construct

**Symptom:** `DeclarationParser` fails on `TypeName varName = expr` because `AssignmentParser`
consumed the tokens first.

**Fix:** `AssignmentParser` must return `null` for patterns that look like declarations
(`TypeName varName = expr`). Let `DeclarationParser` handle them.

---

## 12. Generic type arguments in parser

**Symptom:** Parser hangs or errors on `new HashMap<String, Integer>()` or method returning
a generic type.

**Fix:** `ConstructorCallParser` and `DeclarationParser`/`MethodParser` must skip (consume and
discard) generic type args `<...>` when encountered after a type name.

---

## 13. Import map / FQN resolution

**Symptom:** User-defined class types in casts, declarations, or method signatures are not
resolved to their fully qualified names.

**Fix:** `ClassParser` populates `ParserContext.importMap` from `import` statements.
`CastParser`, `DeclarationParser`, `MethodParser` resolve short names via `importMap`.

---

## Debugging checklist when a new file fails self-compilation

1. Compile the file alone: `java -jar target/orzo.jar <file> -d /tmp/out`
2. Decompile both: `javap -c /tmp/out/...class` vs `javap -c target/classes/...class`
3. Diff the bytecode — first divergence point reveals the bug category above.
4. Check `target/failsafe-reports/*.txt` for the full error after `mvn clean verify`.
5. For infinite loops, run `find_loop.sh` to binary-search the culprit file.

