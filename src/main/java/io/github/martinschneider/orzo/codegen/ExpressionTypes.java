package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_NOT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESSEQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NEGATE;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NOTEQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POW;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.REAL_DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;

import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.tree.BinaryNode;
import io.github.martinschneider.orzo.parser.tree.CastNode;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.InstanceOfNode;
import io.github.martinschneider.orzo.parser.tree.LiteralNode;
import io.github.martinschneider.orzo.parser.tree.PostfixLowering;
import io.github.martinschneider.orzo.parser.tree.TernaryNode;
import io.github.martinschneider.orzo.parser.tree.UnaryNode;
import java.util.List;

/**
 * The types of expression trees, determined bottom-up like in Java: e.g. long for "i + 1L", String
 * for "s + i" and boolean for "a < b". The operators, casts, instanceof and conditional expressions
 * are typed here (see isOperation()), the other expressions (names, calls, ...) by the
 * NumExprTypeDecider.
 */
public class ExpressionTypes {
  private static final List<String> NUMERIC = List.of(BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE);
  private static final List<String> PROMOTED = List.of(INT, LONG, FLOAT, DOUBLE);
  private CGContext ctx;
  private PostfixLowering lowering = new PostfixLowering();

  public ExpressionTypes(CGContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Whether the tree's root is an operation typed (and generated, see ExpressionTreeGenerator) from
   * the tree: an operator (except for the assignments and increments), a cast, instanceof or a
   * conditional expression.
   */
  public static boolean isOperation(ExprNode node) {
    if (node instanceof BinaryNode) {
      return !isAssignment(((BinaryNode) node).op);
    } else if (node instanceof UnaryNode) {
      Operators op = ((UnaryNode) node).op;
      return op == MINUS || op == NEGATE || op == BITWISE_NOT;
    }
    return node instanceof TernaryNode
        || node instanceof InstanceOfNode
        || node instanceof CastNode;
  }

  // "=", "+=", ... (the operators with the lowest precedence)
  public static boolean isAssignment(Operators op) {
    return op.getPrecedence() == Operators.ASSIGN.getPrecedence();
  }

  public static boolean isComparison(Operators op) {
    return op == EQUAL
        || op == NOTEQUAL
        || op == LESS
        || op == LESSEQ
        || op == GREATER
        || op == GREATEREQ;
  }

  public static boolean isShift(Operators op) {
    return op == LSHIFT || op == RSHIFT || op == RSHIFTU;
  }

  public String typeOf(ExprNode node) {
    if (node instanceof LiteralNode) {
      return literalType(((LiteralNode) node).value);
    } else if (!isOperation(node)) {
      return new NumExprTypeDecider(ctx).getType(ctx.classIdMap, postfix(node));
    } else if (node instanceof UnaryNode) {
      UnaryNode unary = (UnaryNode) node;
      if (unary.op == NEGATE) {
        return BOOLEAN;
      }
      return promote(typeOf(unary.operand));
    } else if (node instanceof BinaryNode) {
      return binaryType((BinaryNode) node);
    } else if (node instanceof TernaryNode) {
      TernaryNode ternary = (TernaryNode) node;
      return conditionalType(typeOf(ternary.ifTrue), typeOf(ternary.ifFalse), null);
    } else if (node instanceof CastNode) {
      return ((CastNode) node).type;
    }
    return BOOLEAN; // instanceof
  }

  private String binaryType(BinaryNode binary) {
    Operators op = binary.op;
    if (isComparison(op) || op == LOGICAL_AND || op == LOGICAL_OR) {
      return BOOLEAN;
    }
    String left = typeOf(binary.left);
    String right = typeOf(binary.right);
    if (op == PLUS && (isString(left) || isString(right))) {
      return STRING;
    } else if (op == REAL_DIV) {
      return DOUBLE;
    } else if (op == POW) {
      return (isFloating(left) || isFloating(right)) ? DOUBLE : LONG;
    } else if (isShift(op)) {
      return promote(left);
    } else if ((op == BITWISE_AND || op == BITWISE_OR || op == BITWISE_XOR)
        && isBoolean(left)
        && isBoolean(right)) {
      return BOOLEAN;
    }
    return promote(left, right);
  }

  /** The type of "c ? a : b" (or the given target type, if it is a reference type). */
  public static String conditionalType(String a, String b, String target) {
    if (a == null || NumExprTypeDecider.NULL_TYPE.equals(a)) {
      return b;
    } else if (b == null || NumExprTypeDecider.NULL_TYPE.equals(b) || a.equals(b)) {
      return a;
    } else if (isNumeric(a) && isNumeric(b)) {
      return promote(a, b);
    } else if (isBoolean(a) && isBoolean(b)) {
      return BOOLEAN;
    } else if (target != null && !TypeUtils.isPrimitive(target)) {
      return target;
    }
    return "java.lang.Object";
  }

  public static String literalType(Token literal) {
    if (literal instanceof IntLiteral) {
      IntLiteral number = (IntLiteral) literal;
      long value = number.intValue();
      if (number.isLong || value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
        return LONG;
      }
      return INT;
    } else if (literal instanceof FPLiteral) {
      return ((FPLiteral) literal).isFloat ? FLOAT : DOUBLE;
    } else if (literal instanceof BoolLiteral) {
      return BOOLEAN;
    } else if (literal instanceof Chr) {
      return CHAR;
    }
    return STRING;
  }

  /** The postfix form of a tree, generated from its tokens (not from the tree). */
  public Expression postfix(ExprNode node) {
    Expression expr = lowering.lower(node);
    if (expr.tree == null) {
      return expr; // (e.g. an ArrayInit)
    }
    return new Expression(expr.tokens, expr.cast);
  }

  /** A numeric type or its wrapper class, e.g. "int" or "java.lang.Integer". */
  public static boolean isNumeric(String type) {
    return type != null && NUMERIC.contains(unboxed(type));
  }

  /** A numeric type (or wrapper) that isn't a floating-point one, e.g. int or java.lang.Long. */
  public static boolean isIntegral(String type) {
    return isNumeric(type) && !isFloating(type);
  }

  public static boolean isFloating(String type) {
    String unboxed = unboxed(type);
    return FLOAT.equals(unboxed) || DOUBLE.equals(unboxed);
  }

  public static boolean isBoolean(String type) {
    return BOOLEAN.equals(type) || "java.lang.Boolean".equals(type);
  }

  public static boolean isString(String type) {
    return STRING.equals(type) || "java.lang.String".equals(type);
  }

  /** The primitive type of a wrapper class (e.g. int for java.lang.Integer), otherwise the type. */
  public static String unboxed(String type) {
    String unboxed = TypeUtils.unboxedType(type);
    return (unboxed != null) ? unboxed : type;
  }

  /** Unary numeric promotion (JLS 5.6), e.g. int for a byte or a java.lang.Short. */
  public static String promote(String type) {
    String unboxed = unboxed(type);
    if (unboxed != null && PROMOTED.contains(unboxed)) {
      return unboxed;
    }
    return INT;
  }

  /** Binary numeric promotion (JLS 5.6), e.g. long for (int, long). */
  public static String promote(String a, String b) {
    int indexA = PROMOTED.indexOf(promote(a));
    int indexB = PROMOTED.indexOf(promote(b));
    return PROMOTED.get(Math.max(indexA, indexB));
  }
}
