package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.conditionalType;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.isBoolean;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.isComparison;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.isFloating;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.isNumeric;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.isShift;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.isString;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.literalType;
import static io.github.martinschneider.orzo.codegen.ExpressionTypes.promote;
import static io.github.martinschneider.orzo.codegen.OpCodes.ACONST_NULL;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.OpCodes.DDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.DNEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.FNEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.OpCodes.IAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ACMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ACMPNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.INEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.INSTANCEOF;
import static io.github.martinschneider.orzo.codegen.OpCodes.IOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IXOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LNEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.LXOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.POP;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.ARITHMETIC_OPS;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.COMPARE_TO_ZERO_OPS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESSEQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NEGATE;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NOTEQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POW;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.REAL_DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.ExpressionTypes;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.tree.BinaryNode;
import io.github.martinschneider.orzo.parser.tree.CastNode;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.InstanceOfNode;
import io.github.martinschneider.orzo.parser.tree.LiteralNode;
import io.github.martinschneider.orzo.parser.tree.OpaqueNode;
import io.github.martinschneider.orzo.parser.tree.PostfixLowering;
import io.github.martinschneider.orzo.parser.tree.TernaryNode;
import io.github.martinschneider.orzo.parser.tree.UnaryNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Generates the code of an operation (see ExpressionTypes.isOperation()) from its expression tree:
 * the operands are typed bottom-up (with binary numeric promotion, string concatenation, ...) and
 * the result is converted to the requested type. Any other expression (a name, field access, call,
 * object creation, lambda, assignment or increment) is generated from its postfix form by the
 * ExpressionGenerator.
 */
public class ExpressionTreeGenerator {
  private static final String LOG_NAME = "expression code generator";
  // the branch opcodes and their negations
  private static final Map<Byte, Byte> NEGATED_BRANCHES =
      Map.ofEntries(
          Map.entry(IFEQ, IFNE),
          Map.entry(IFNE, IFEQ),
          Map.entry(IFLT, IFGE),
          Map.entry(IFGE, IFLT),
          Map.entry(IFGT, IFLE),
          Map.entry(IFLE, IFGT),
          Map.entry(IF_ICMPEQ, IF_ICMPNE),
          Map.entry(IF_ICMPNE, IF_ICMPEQ),
          Map.entry(IF_ICMPLT, IF_ICMPGE),
          Map.entry(IF_ICMPGE, IF_ICMPLT),
          Map.entry(IF_ICMPGT, IF_ICMPLE),
          Map.entry(IF_ICMPLE, IF_ICMPGT),
          Map.entry(IF_ACMPEQ, IF_ACMPNE),
          Map.entry(IF_ACMPNE, IF_ACMPEQ));
  private CGContext ctx;
  private ExpressionTypes types;

  public ExpressionTreeGenerator(CGContext ctx) {
    this.ctx = ctx;
    types = new ExpressionTypes(ctx);
  }

  /**
   * Generates an operation. As a condition (pushIfZero false, see ExpressionGenerator.eval()), the
   * code ends with a branch opcode (without its offset) that jumps if the condition is true, or
   * false for reverseComp. Otherwise, it leaves the value, converted to the given type (if it is a
   * primitive type: boxing is up to the caller).
   */
  public ExpressionResult generate(
      DynamicByteArray out, String type, ExprNode node, boolean pushIfZero, boolean reverseComp) {
    if (!pushIfZero || reverseComp) {
      checkDivisions(node, false);
      branch(out, node, !reverseComp);
      return new ExpressionResult(BOOLEAN, null);
    }
    checkDivisions(node, isFloating(type));
    String valueType = value(out, node, type);
    return new ExpressionResult(convertPrimitive(out, valueType, type), null);
  }

  // Warns about an integer division whose result is used as a floating-point value, e.g. "1 / 4"
  // in "double d = 1 / 4;" (0, like in Java, unlike "1 ÷ 4")
  private void checkDivisions(ExprNode node, boolean floatingContext) {
    if (node instanceof UnaryNode && ((UnaryNode) node).op == MINUS) {
      checkDivisions(((UnaryNode) node).operand, floatingContext);
    } else if (node instanceof TernaryNode) {
      checkDivisions(((TernaryNode) node).ifTrue, floatingContext);
      checkDivisions(((TernaryNode) node).ifFalse, floatingContext);
    } else if (node instanceof BinaryNode) {
      BinaryNode binary = (BinaryNode) node;
      String type = types.typeOf(node);
      if (binary.op == DIV && floatingContext && ExpressionTypes.isIntegral(type)) {
        ctx.errors.addWarning(
            LOG_NAME,
            "integer division in a floating-point context (use ÷ to divide in floating point)",
            new RuntimeException().getStackTrace());
      }
      boolean operandsFloating = false;
      if (isComparison(binary.op)) {
        operandsFloating =
            isFloating(types.typeOf(binary.left)) || isFloating(types.typeOf(binary.right));
      } else if (isArithmetic(binary.op) && isNumeric(type)) {
        operandsFloating = floatingContext || isFloating(type);
      }
      checkDivisions(binary.left, operandsFloating);
      checkDivisions(binary.right, operandsFloating);
    }
  }

  private static boolean isArithmetic(Operators op) {
    return op == PLUS || op == MINUS || op == TIMES || op == DIV || op == MOD || op == POW;
  }

  // Generates the value of an expression, returns its type. The target type is only used where the
  // type of an expression depends on it (e.g. a lambda).
  private String value(DynamicByteArray out, ExprNode node, String target) {
    if (node instanceof LiteralNode) {
      return literal(out, ((LiteralNode) node).value, null);
    } else if (!ExpressionTypes.isOperation(node)) {
      return postfix(out, node, (node instanceof OpaqueNode) ? target : null);
    } else if (node instanceof UnaryNode) {
      return unary(out, (UnaryNode) node);
    } else if (node instanceof BinaryNode) {
      return binary(out, (BinaryNode) node);
    } else if (node instanceof TernaryNode) {
      return conditional(out, (TernaryNode) node, target);
    } else if (node instanceof CastNode) {
      CastNode cast = (CastNode) node;
      String operandType = value(out, cast.operand, cast.type);
      ctx.basicGen.checkCastable(null, operandType, cast.type);
      ctx.basicGen.convert(out, operandType, cast.type);
      return cast.type;
    }
    InstanceOfNode test = (InstanceOfNode) node;
    if (test.binding != null) {
      return pattern(out, test);
    }
    value(out, test.operand, null);
    String jvmType = ctx.exprGen.resolveJvmClassName(test.type);
    ctx.constPool.addClass(jvmType);
    out.write(INSTANCEOF);
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, jvmType));
    replaceTop(BOOLEAN);
    return BOOLEAN;
  }

  // "o instanceof T v": v = (T) o if the test succeeds (v is a new local variable, initialized
  // with null first, so that it has the same type on all paths)
  private String pattern(DynamicByteArray out, InstanceOfNode test) {
    String jvmType = ctx.exprGen.resolveJvmClassName(test.type);
    String type = "java/lang/String".equals(jvmType) ? STRING : jvmType.replace('/', '.');
    VariableInfo var =
        new VariableInfo(
            test.binding,
            type,
            emptyList(),
            false,
            (short) ctx.classIdMap.variables.localSize,
            null);
    ctx.classIdMap.variables.putLocal(Token.id(test.binding), var);
    out.write(ACONST_NULL);
    ctx.opStack.push(type);
    ctx.storeGen.store(out, var);
    value(out, test.operand, null);
    out.write(DUP);
    ctx.opStack.push(REF);
    ctx.constPool.addClass(jvmType);
    out.write(INSTANCEOF);
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, jvmType));
    replaceTop(BOOLEAN);
    int branch = out.size();
    out.write(IFEQ);
    out.write((short) 0); // (the offset, see below)
    ctx.opStack.pop();
    out.write(CHECKCAST);
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, jvmType));
    replaceTop(type);
    ctx.storeGen.store(out, var);
    out.write(ICONST_1);
    int jump = out.size();
    out.write(GOTO);
    out.write((short) 0);
    int failed = out.size();
    out.write(POP);
    out.write(ICONST_0);
    out.patch(branch + 1, shortToByteArray((short) (failed - branch)));
    out.patch(jump + 1, shortToByteArray((short) (out.size() - jump)));
    ctx.opStack.push(BOOLEAN);
    return BOOLEAN;
  }

  // the value of an expression converted to the given type (a literal is pushed as that type)
  private String valueAs(DynamicByteArray out, ExprNode node, String type) {
    if (node instanceof LiteralNode && TypeUtils.isPrimitive(type)) {
      Token literal = ((LiteralNode) node).value;
      if (isNumeric(type) && !(literal instanceof BoolLiteral) && !(literal instanceof Str)) {
        return literal(out, literal, type);
      }
    }
    String valueType = value(out, node, type);
    if (TypeUtils.isPrimitive(type) || TypeUtils.isPrimitive(valueType)) {
      if (!type.equals(valueType)) {
        ctx.basicGen.convert1(out, valueType, type); // (also boxing and unboxing)
        replaceTop(type);
      }
      return type;
    }
    return valueType;
  }

  // Converts a value of the given type to a primitive type (a numeric conversion or unboxing).
  // Returns the resulting type.
  private String convertPrimitive(DynamicByteArray out, String from, String to) {
    if (to == null || from == null || to.equals(from) || !TypeUtils.isPrimitive(to)) {
      return from;
    } else if (BOOLEAN.equals(to) != isBoolean(from)) {
      return from; // (an error reported by the caller)
    } else if (!TypeUtils.isPrimitive(from) && TypeUtils.unboxedType(from) == null) {
      return from;
    }
    ctx.basicGen.convert1(out, from, to);
    replaceTop(to);
    return to;
  }

  // a literal, as the given numeric type (or its own type if that is null)
  private String literal(DynamicByteArray out, Token literal, String type) {
    if (literal instanceof IntLiteral || literal instanceof FPLiteral || literal instanceof Chr) {
      String pushType = (type != null) ? type : literalType(literal);
      BigDecimal value;
      if (literal instanceof IntLiteral) {
        value = BigDecimal.valueOf(((IntLiteral) literal).intValue());
      } else if (literal instanceof FPLiteral) {
        value = (BigDecimal) literal.val;
      } else {
        int chr = (char) literal.val;
        value = BigDecimal.valueOf(chr);
      }
      ctx.pushGen.push(out, pushType, value);
      return pushType;
    } else if (literal instanceof BoolLiteral) {
      ctx.pushGen.pushBool(out, Boolean.TRUE.equals(literal.val));
      return BOOLEAN;
    }
    ctx.loadGen.ldc(out, CONSTANT_STRING, ((Str) literal).strValue());
    return STRING;
  }

  // An expression that isn't an operation, from its postfix form (see ExpressionGenerator)
  private String postfix(DynamicByteArray out, ExprNode node, String target) {
    ExpressionResult result = ctx.exprGen.eval(out, target, types.postfix(node));
    if (result == null || result.type == null) {
      return REF;
    }
    return result.type;
  }

  private String unary(DynamicByteArray out, UnaryNode unary) {
    if (unary.op == NEGATE) {
      valueAs(out, unary.operand, BOOLEAN);
      out.write(ICONST_1);
      out.write(IXOR);
      return BOOLEAN;
    }
    if (unary.op == MINUS && unary.operand instanceof LiteralNode) {
      Token literal = ((LiteralNode) unary.operand).value;
      if (literal instanceof IntLiteral || literal instanceof FPLiteral) {
        return literal(out, PostfixLowering.negate(literal), null);
      }
    }
    String type = promote(types.typeOf(unary.operand));
    valueAs(out, unary.operand, type);
    if (unary.op == MINUS) {
      if (LONG.equals(type)) {
        out.write(LNEG);
      } else if (FLOAT.equals(type)) {
        out.write(FNEG);
      } else if (DOUBLE.equals(type)) {
        out.write(DNEG);
      } else {
        out.write(INEG);
      }
    } else if (LONG.equals(type)) {
      // "~x" is "x ^ -1"
      ctx.pushGen.push(out, LONG, BigDecimal.valueOf(-1));
      out.write(LXOR);
      ctx.opStack.pop();
    } else {
      out.write(ICONST_M1);
      out.write(IXOR);
    }
    return type;
  }

  private String binary(DynamicByteArray out, BinaryNode binary) {
    Operators op = binary.op;
    if (op == LOGICAL_AND || op == LOGICAL_OR) {
      return shortCircuit(out, binary);
    } else if (isComparison(op)) {
      // a 0 or 1, e.g. for "boolean b = x < y;"
      branch(out, binary, true);
      out.write((short) 7); // to the ICONST_1
      out.write(ICONST_0);
      out.write(GOTO);
      out.write((short) 4);
      out.write(ICONST_1);
      ctx.opStack.push(BOOLEAN);
      return BOOLEAN;
    }
    String left = types.typeOf(binary.left);
    String right = types.typeOf(binary.right);
    if (op == PLUS && (isString(left) || isString(right))) {
      return concatenation(out, binary);
    } else if (op == POW) {
      return pow(out, binary, isFloating(left) || isFloating(right));
    } else if (op == REAL_DIV) {
      valueAs(out, binary.left, DOUBLE);
      valueAs(out, binary.right, DOUBLE);
      out.write(DDIV);
      ctx.opStack.pop();
      return DOUBLE;
    } else if ((op == BITWISE_AND || op == BITWISE_OR || op == BITWISE_XOR)
        && isBoolean(left)
        && isBoolean(right)) {
      // non-short-circuiting, e.g. "a != null & b != null"
      valueAs(out, binary.left, BOOLEAN);
      valueAs(out, binary.right, BOOLEAN);
      out.write((op == BITWISE_AND) ? IAND : ((op == BITWISE_OR) ? IOR : IXOR));
      ctx.opStack.pop();
      return BOOLEAN;
    }
    if (!isNumeric(left) || !isNumeric(right)) {
      return badOperands(out, binary);
    }
    String type = operands(out, binary);
    byte[] opCode = ARITHMETIC_OPS.get(op).get(type);
    if (opCode == null) {
      return badOperands(out, binary);
    }
    out.write(opCode);
    ctx.opStack.pop2();
    ctx.opStack.push(type);
    return type;
  }

  private String badOperands(DynamicByteArray out, BinaryNode binary) {
    ctx.errors.addError(
        LOG_NAME,
        "bad operand types for binary operator " + Token.op(binary.op).display(),
        new RuntimeException().getStackTrace());
    return REF;
  }

  // "a && b" and "a || b" as a 0 or 1: b is only evaluated if a is true (or false for "||")
  private String shortCircuit(DynamicByteArray out, BinaryNode binary) {
    boolean and = binary.op == LOGICAL_AND;
    valueAs(out, binary.left, BOOLEAN);
    int branch = out.size();
    out.write(and ? IFEQ : IFNE);
    out.write((short) 0); // (the offset, see below)
    ctx.opStack.pop();
    valueAs(out, binary.right, BOOLEAN);
    out.write(GOTO);
    out.write((short) 4);
    int constant = out.size();
    out.write(and ? ICONST_0 : ICONST_1);
    out.patch(branch + 1, shortToByteArray((short) (constant - branch)));
    return BOOLEAN;
  }

  // "s + x" or "x + s" for a String s: both operands as Strings, then String.concat()
  private String concatenation(DynamicByteArray out, BinaryNode binary) {
    concatOperand(out, binary.left);
    concatOperand(out, binary.right);
    ctx.invokeGen.invokeVirtual(
        out, new Method("java.lang.String", "concat", STRING, List.of(STRING)));
    return STRING;
  }

  // (a String that may be null becomes "null", like an Object: String.concat() would throw)
  private void concatOperand(DynamicByteArray out, ExprNode node) {
    String type = value(out, node, null);
    boolean nonNull =
        (node instanceof LiteralNode && ((LiteralNode) node).value instanceof Str)
            || (node instanceof BinaryNode && ((BinaryNode) node).op == PLUS);
    toString(out, (isString(type) && !nonNull) ? REF : type);
  }

  private void toString(DynamicByteArray out, String type) {
    if (isString(type)) {
      return;
    } else if (TypeUtils.isPrimitive(type)) {
      ctx.basicGen.primitiveToString(out, type);
      replaceTop(STRING);
    } else {
      ctx.invokeGen.invokeStatic(
          out, new Method("java.lang.String", "valueOf", STRING, List.of("java.lang.Object")));
    }
  }

  // "a ** b": Math.pow() for a floating-point operand, otherwise
  // BigInteger.valueOf(a).pow(b).longValue()
  private String pow(DynamicByteArray out, BinaryNode binary, boolean floating) {
    if (floating) {
      valueAs(out, binary.left, DOUBLE);
      valueAs(out, binary.right, DOUBLE);
      ctx.constPool.addClass("java/lang/Math");
      ctx.invokeGen.invokeStatic(
          out, new Method("java.lang.Math", "pow", DOUBLE, List.of(DOUBLE, DOUBLE)));
      return DOUBLE;
    }
    ctx.constPool.addClass("java/math/BigInteger");
    ctx.constPool.addMethodRef("java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;");
    ctx.constPool.addMethodRef("java/math/BigInteger", "pow", "(I)Ljava/math/BigInteger;");
    ctx.constPool.addMethodRef("java/math/BigInteger", "longValue", "()J");
    valueAs(out, binary.left, LONG);
    ctx.invokeGen.invokeStatic(
        out,
        new Method("java.math.BigInteger", "valueOf", "Ljava/math/BigInteger;", List.of(LONG)));
    valueAs(out, binary.right, INT);
    ctx.invokeGen.invokeVirtual(
        out, new Method("java/math/BigInteger", "pow", "Ljava/math/BigInteger;", List.of(INT)));
    ctx.invokeGen.invokeVirtual(
        out, new Method("java/math/BigInteger", "longValue", LONG, new ArrayList<>()));
    return LONG;
  }

  // "c ? a : b"
  private String conditional(DynamicByteArray out, TernaryNode conditional, String target) {
    String type =
        conditionalType(
            types.typeOf(conditional.ifTrue), types.typeOf(conditional.ifFalse), target);
    branch(out, conditional.condition, false);
    int toFalse = out.size() - 1;
    out.write((short) 0); // (the offsets, see below)
    valueAs(out, conditional.ifTrue, type);
    int toEnd = out.size();
    out.write(GOTO);
    out.write((short) 0);
    out.patch(toFalse + 1, shortToByteArray((short) (out.size() - toFalse)));
    ctx.opStack.pop(); // (the value of the false branch replaces it)
    valueAs(out, conditional.ifFalse, type);
    out.patch(toEnd + 1, shortToByteArray((short) (out.size() - toEnd)));
    return type;
  }

  // A condition: the code ends with a branch opcode (without its offset) that jumps if the
  // condition is jumpIf.
  private void branch(DynamicByteArray out, ExprNode node, boolean jumpIf) {
    if (node instanceof UnaryNode && ((UnaryNode) node).op == NEGATE) {
      branch(out, ((UnaryNode) node).operand, !jumpIf);
      return;
    } else if (node instanceof BinaryNode && isComparison(((BinaryNode) node).op)) {
      compare(out, (BinaryNode) node, jumpIf);
      return;
    }
    valueAs(out, node, BOOLEAN);
    out.write(jumpIf ? IFNE : IFEQ);
    ctx.opStack.pop();
  }

  private void compare(DynamicByteArray out, BinaryNode comparison, boolean jumpIf) {
    Operators op = comparison.op;
    byte[] opCode = null;
    ExprNode zeroComparedTo = null;
    Operators zeroOp = op;
    if (isZero(comparison.right) && !isZero(comparison.left)) {
      zeroComparedTo = comparison.left; // e.g. "x > 0": IFGT
    } else if (isZero(comparison.left) && !isZero(comparison.right)) {
      zeroComparedTo = comparison.right; // e.g. "0 < x" is "x > 0"
      zeroOp = mirrored(op);
    }
    if (zeroComparedTo != null && isIntLike(types.typeOf(zeroComparedTo))) {
      String type = value(out, zeroComparedTo, null);
      if (isIntLike(type)) {
        opCode = new byte[] {COMPARE_TO_ZERO_OPS.get(zeroOp)};
        ctx.opStack.pop();
      } else {
        // (mispredicted, e.g. a long)
        String promoted = operationType(zeroOp, type, INT);
        convertOperand(out, type, promoted);
        ExprNode zero = (zeroComparedTo == comparison.left) ? comparison.right : comparison.left;
        literal(out, ((LiteralNode) zero).value, promoted);
        opCode = ARITHMETIC_OPS.get(zeroOp).get(promoted);
        ctx.opStack.pop2();
      }
    }
    if (opCode == null) {
      opCode = ARITHMETIC_OPS.get(op).get(operands(out, comparison));
      ctx.opStack.pop2();
    }
    if (opCode == null) {
      badOperands(out, comparison);
      opCode = new byte[] {IF_ACMPEQ};
    }
    opCode = Arrays.copyOf(opCode, opCode.length);
    if (!jumpIf) {
      opCode[opCode.length - 1] = NEGATED_BRANCHES.get(opCode[opCode.length - 1]);
    }
    out.write(opCode);
  }

  // Generates the operands of a binary operation, converted to the type of the operation, and
  // returns that type (REF for a comparison of references). (The type of the right operand is
  // predicted, see ExpressionTypes, and corrected if it turns out to be wider.)
  private String operands(DynamicByteArray out, BinaryNode binary) {
    Operators op = binary.op;
    String rightType = types.typeOf(binary.right);
    String type;
    if (binary.left instanceof LiteralNode) {
      type = operationType(op, types.typeOf(binary.left), rightType);
      operand(out, binary.left, type);
    } else {
      String leftType = value(out, binary.left, null);
      type = operationType(op, leftType, rightType);
      convertOperand(out, leftType, type);
    }
    // (for a shift, the right operand is an int)
    String rightTarget = isShift(op) ? INT : type;
    if (binary.right instanceof LiteralNode) {
      operand(out, binary.right, rightTarget);
      return type;
    }
    String actual = value(out, binary.right, null);
    if (!isShift(op) && !REF.equals(type)) {
      String corrected = operationType(op, type, actual);
      if (!corrected.equals(type)) {
        ctx.exprGen.convertBelowTop(out, actual, type, corrected);
        type = corrected;
        rightTarget = type;
      }
    }
    convertOperand(out, actual, rightTarget);
    return type;
  }

  // the type of a binary operation on operands of the given types: the promoted type (e.g. long
  // for "int + long"), for a comparison also BOOLEAN or REF (references)
  private static String operationType(Operators op, String left, String right) {
    if (isComparison(op)) {
      // (two wrapper objects are compared as references by "==" and "!=")
      boolean references =
          (op == EQUAL || op == NOTEQUAL)
              && !TypeUtils.isPrimitive(left)
              && !TypeUtils.isPrimitive(right);
      if (references) {
        return REF;
      } else if (isNumeric(left) && isNumeric(right)) {
        return promote(left, right);
      } else if (isBoolean(left) && isBoolean(right)) {
        return BOOLEAN;
      }
      return REF;
    } else if (isShift(op)) {
      return promote(left);
    }
    return promote(left, right);
  }

  // an operand of the given type (REF: of its own reference type)
  private void operand(DynamicByteArray out, ExprNode node, String type) {
    if (REF.equals(type)) {
      value(out, node, null);
    } else {
      valueAs(out, node, type);
    }
  }

  private void convertOperand(DynamicByteArray out, String from, String to) {
    if (!REF.equals(to) && !to.equals(from)) {
      ctx.basicGen.convert1(out, from, to);
      replaceTop(to);
    }
  }

  private static boolean isIntLike(String type) {
    return isNumeric(type) && INT.equals(promote(type)) && TypeUtils.isPrimitive(type);
  }

  // the comparison with swapped operands, e.g. ">" for "<"
  private static Operators mirrored(Operators op) {
    if (op == LESS) {
      return GREATER;
    } else if (op == LESSEQ) {
      return GREATEREQ;
    } else if (op == GREATER) {
      return LESS;
    } else if (op == GREATEREQ) {
      return LESSEQ;
    }
    return op;
  }

  private static boolean isZero(ExprNode node) {
    if (!(node instanceof LiteralNode)) {
      return false;
    }
    Token literal = ((LiteralNode) node).value;
    return literal instanceof IntLiteral
        && !((IntLiteral) literal).isLong
        && ((IntLiteral) literal).intValue() == 0;
  }

  // the type of the value on top of the stack changes, e.g. after a conversion
  private void replaceTop(String type) {
    ctx.opStack.pop();
    ctx.opStack.push(type);
  }
}
