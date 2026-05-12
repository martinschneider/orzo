# Orzo Code Review Tracker

Tracks the review status of source files relevant to self-compilation.
Focus: dead code, hardcoded shortcuts, scalability issues.

Status: **REVIEWED** | **PENDING** | **SKIP** (generated/trivial)

---

## Parser

|               File                |  Status  |                                                         Findings                                                         |           Fixed            |    TODO ref     |
|-----------------------------------|----------|--------------------------------------------------------------------------------------------------------------------------|----------------------------|-----------------|
| `parser/ArraySelectorParser.java` | REVIEWED | Chained call statement + assignment-in-condition prevented Orzo self-parse                                               | Yes — refactored           | —               |
| `parser/ArrayDefParser.java`      | REVIEWED | No issues found                                                                                                          | —                          | —               |
| `parser/BreakParser.java`         | REVIEWED | No issues found                                                                                                          | —                          | —               |
| `parser/FloorParser.java`         | REVIEWED | No issues found                                                                                                          | —                          | —               |
| `parser/ClassParser.java`         | REVIEWED | Bootstrap workaround for generic type params; annotation skipping (scanAnnotation) could miss annotations with arguments | No                         | TODO #bootstrap |
| `parser/DeclarationParser.java`   | REVIEWED | Bootstrap `NoSuchFieldError` workaround; no-init reference declarations exposed DeclarationGenerator bug                 | DeclarationGenerator fixed | TODO #bootstrap |
| `parser/MethodParser.java`        | REVIEWED | Bootstrap `NoSuchFieldError` workaround                                                                                  | No                         | TODO #bootstrap |
| `parser/StatementParser.java`     | PENDING  | —                                                                                                                        | —                          | —               |
| `parser/ExpressionParser.java`    | PENDING  | Long complex method, TODO "simplify"                                                                                     | —                          | TODO            |
| `parser/ParserContext.java`       | PENDING  | —                                                                                                                        | —                          | —               |
| `parser/ProdParser.java`          | REVIEWED | Interface only, no issues                                                                                                | —                          | —               |

## Parser Productions (data classes)

|                  File                  |  Status  |                 Findings                  |
|----------------------------------------|----------|-------------------------------------------|----|------|
| `productions/AccessFlag.java`          | REVIEWED | No issues                                 |
| `productions/Argument.java`            | REVIEWED | No issues                                 |
| `productions/ArrayInit.java`           | REVIEWED | No issues                                 |
| `productions/ArraySelector.java`       | REVIEWED | No issues                                 |
| `productions/Assignment.java`          | REVIEWED | No issues                                 |
| `productions/Break.java`               | REVIEWED | No issues                                 |
| `productions/ClassMember.java`         | REVIEWED | No issues                                 |
| `productions/Constructor.java`         | REVIEWED | No issues                                 |
| `productions/ConstructorCall.java`     | REVIEWED | No issues                                 |
| `productions/Declaration.java`         | REVIEWED | No issues                                 |
| `productions/DoStatement.java`         | REVIEWED | No issues                                 |
| `productions/EmptyStatement.java`      | REVIEWED | No issues                                 |
| `productions/FieldSelector.java`       | REVIEWED | No issues                                 |
| `productions/ForStatement.java`        | REVIEWED | No issues                                 |
| `productions/IfBlock.java`             | REVIEWED | No issues                                 |
| `productions/IfStatement.java`         | REVIEWED | No issues                                 |
| `productions/Import.java`              | REVIEWED | No issues                                 |
| `productions/IncrementStatement.java`  | REVIEWED | No issues                                 |
| `productions/LoopStatement.java`       | REVIEWED | No issues                                 |
| `productions/MethodCall.java`          | REVIEWED | No issues                                 |
| `productions/Method.java`              | REVIEWED | `accessFlags()` has TODO for general case | No | TODO |
| `productions/ParallelDeclaration.java` | REVIEWED | No issues                                 |
| `productions/ReturnStatement.java`     | REVIEWED | No issues                                 |
| `productions/Selector.java`            | REVIEWED | No issues                                 |
| `productions/Statement.java`           | REVIEWED | No issues                                 |
| `productions/WhileStatement.java`      | REVIEWED | No issues                                 |

## Lexer Tokens

|              File               |  Status  |                                                        Findings                                                        |
|---------------------------------|----------|------------------------------------------------------------------------------------------------------------------------|----|------------|
| `tokens/BoolLiteral.java`       | REVIEWED | No issues                                                                                                              |
| `tokens/Chr.java`               | REVIEWED | No issues                                                                                                              |
| `tokens/EOF.java`               | REVIEWED | No issues                                                                                                              |
| `tokens/Identifier.java`        | REVIEWED | No issues                                                                                                              |
| `tokens/Keyword.java`           | REVIEWED | No issues                                                                                                              |
| `tokens/Keywords.java`          | REVIEWED | No issues                                                                                                              |
| `tokens/Num.java`               | REVIEWED | No issues                                                                                                              |
| `tokens/Operators.java`         | REVIEWED | No issues                                                                                                              |
| `tokens/Scope.java`             | REVIEWED | No issues                                                                                                              |
| `tokens/Scopes.java`            | REVIEWED | `accFlag` field causes `NoSuchFieldError` during bootstrap; the field itself is fine — it's a bootstrap ordering issue |
| `tokens/Str.java`               | REVIEWED | No issues                                                                                                              |
| `tokens/Sym.java`               | REVIEWED | No issues                                                                                                              |
| `tokens/Symbols.java`           | REVIEWED | No issues                                                                                                              |
| `tokens/TernaryExpression.java` | REVIEWED | No issues                                                                                                              |
| `tokens/Token.java`             | REVIEWED | No issues                                                                                                              |
| `tokens/Type.java`              | REVIEWED | TODO in file: "replace this with a simple string"                                                                      | No | TODO #type |

## Codegen

|               File                |  Status  |                                                    Findings                                                    |        Fixed         | TODO ref |
|-----------------------------------|----------|----------------------------------------------------------------------------------------------------------------|----------------------|----------|
| `codegen/ArrayTypes.java`         | REVIEWED | No issues                                                                                                      | —                    | —        |
| `codegen/ByteUtils.java`          | REVIEWED | No issues                                                                                                      | —                    | —        |
| `codegen/CGContext.java`          | REVIEWED | No issues                                                                                                      | —                    | —        |
| `codegen/CodeGenerator.java`      | REVIEWED | `supportPrint()` unconditionally adds System/PrintStream to const pool; major version 52 without StackMapTable | Documented in README | TODO     |
| `codegen/DynamicByteArray.java`   | REVIEWED | TODO: "variable declaration with a method call doesn't work"                                                   | No                   | TODO     |
| `codegen/ExpressionResult.java`   | REVIEWED | No issues                                                                                                      | —                    | —        |
| `codegen/FieldProcessor.java`     | REVIEWED | Hardcoded java.lang fallback list for simple class names                                                       | No                   | TODO     |
| `codegen/HasOutput.java`          | REVIEWED | No issues                                                                                                      | —                    | —        |
| `codegen/MethodProcessor.java`    | REVIEWED | `addJavaLang()` hardcodes specific classes; static methods only; should reflect imports                        | No                   | TODO     |
| `codegen/NumExprTypeDecider.java` | REVIEWED | Creates new instance recursively — should reuse `this`                                                         | No                   | TODO     |
| `codegen/OpCodes.java`            | REVIEWED | No issues                                                                                                      | —                    | —        |
| `codegen/OperandStack.java`       | REVIEWED | TODO: ideally written differently                                                                              | No                   | TODO     |
| `codegen/TypeUtils.java`          | REVIEWED | TODO: multi-arrays, single check                                                                               | No                   | TODO     |

## Codegen Generators

|                  File                  |  Status  |                                                              Findings                                                              |          Fixed          | TODO ref |
|----------------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------|-------------------------|----------|
| `generators/BasicGenerator.java`       | REVIEWED | Duplicate PRIMITIVE_TYPES set (same as TypeUtils.isPrimitive); hardcoded autobox/primitiveToString descriptors                     | PRIMITIVE_TYPES removed | TODO     |
| `generators/DeclarationGenerator.java` | REVIEWED | Null-init reference declarations emitted no bytecode → VerifyError                                                                 | Fixed (aconst_null)     | —        |
| `generators/DoNothingGenerator.java`   | REVIEWED | No issues                                                                                                                          | —                       | —        |
| `generators/MethodCallGenerator.java`  | REVIEWED | Dead `isPrimitiveType()` method; hardcoded Object methods; hardcoded System.out.println; findMethodViaReflection/Typed duplication | isPrimitiveType removed | TODO     |
| `generators/PushGenerator.java`        | REVIEWED | Missing BOOLEAN case in push() → silent stack underflow                                                                            | Fixed                   | —        |
| `generators/StatementGenerator.java`   | REVIEWED | No issues                                                                                                                          | —                       | —        |

## Codegen Constants

|                    File                     |  Status  |                         Findings                          | Fixed | TODO ref |
|---------------------------------------------|----------|-----------------------------------------------------------|-------|----------|
| `constants/Constant.java`                   | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantClass.java`              | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantDouble.java`             | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantFieldref.java`           | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantFloat.java`              | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantInteger.java`            | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantInterfaceMethodref.java` | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantLong.java`               | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantMethodref.java`          | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantNameAndType.java`        | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantPool.java`               | REVIEWED | L90: Long/Double index may be off by one (size vs size-1) | No    | TODO     |
| `constants/ConstantString.java`             | REVIEWED | No issues                                                 | —     | —        |
| `constants/ConstantTypes.java`              | REVIEWED | No issues                                                 | —     | —        |

## Codegen Identifiers

|                 File                  |  Status  |                       Findings                        | Fixed | TODO ref |
|---------------------------------------|----------|-------------------------------------------------------|-------|----------|
| `identifier/GlobalIdentifierMap.java` | REVIEWED | TODO: temporary split between maps, should merge      | No    | TODO     |
| `identifier/IdentifierInfo.java`      | REVIEWED | No issues                                             | —     | —        |
| `identifier/IdentifierMap.java`       | REVIEWED | TODO: error handling for duplicate variable names     | No    | TODO     |
| `identifier/VariableInfo.java`        | REVIEWED | TODO: support objects other than `this` for objectRef | No    | TODO     |

---

## Not Yet Reviewed (lower priority)

These files exist but are not currently in the allowlist and were not examined during this review pass:

- All remaining `parser/` files (ExpressionParser, StatementParser, etc.)
- All remaining `codegen/generators/` files (ExpressionGenerator, AssignmentGenerator, etc.)
- `lexer/Lexer.java`
- `Orzo.java` (main entry point)

