package io.github.martinschneider.orzo.parser.tree;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_NOT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.REAL_DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;

import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.tokens.CastOperator;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.InstanceofToken;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.TernaryExpression;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns an expression tree into the postfix form the code generator works with, e.g. "a + b * c"
 * into "a b c * +". Besides:
 *
 * <ul>
 *   <li>a chain of selectors becomes a single Identifier token, linked by "next", e.g. "a.b[i].c";
 *       a call takes the names of its receiver into its own name ("a.b.f(x)" is one MethodCall
 *       "a.b.f")
 *   <li>a negation "-x" becomes "-1 * x" (or a negative literal), "~x" becomes "-1 ^ x"
 *   <li>a cast of a whole expression becomes the cast of the Expression, other casts to a primitive
 *       type a CastOperator and other casts to a reference type a CastMethodCall
 *   <li>a parenthesized receiver, e.g. "(a + b)" in "(a + b).length()" or "((Foo) x)" in "((Foo)
 *       x).bar()", becomes a CastMethodCall
 * </ul>
 */
public class PostfixLowering {
  // the kinds of the links of a selector chain (see addChain)
  private static final int OTHER = 0;
  private static final int NAME = 1;
  private static final int TYPE_NAME = 2;
  private static final int CALL = 3;

  public Expression lower(ExprNode node) {
    if (node instanceof OpaqueNode) {
      return ((OpaqueNode) node).expr;
    }
    Expression expr;
    if (node instanceof CastNode) {
      CastNode cast = (CastNode) node;
      expr = new Expression(tokens(cast.operand), new Type(cast.type));
    } else {
      expr = new Expression(tokens(node), null);
    }
    expr.tree = node;
    return expr;
  }

  private List<Expression> lower(List<ExprNode> nodes) {
    List<Expression> exprs = new ArrayList<>();
    for (ExprNode node : nodes) {
      exprs.add(lower(node));
    }
    return exprs;
  }

  private List<Token> tokens(ExprNode node) {
    List<Token> out = new ArrayList<>();
    add(node, out);
    return out;
  }

  private void add(ExprNode node, List<Token> out) {
    if (node instanceof LiteralNode) {
      out.add(((LiteralNode) node).value);
    } else if (node instanceof UnaryNode) {
      addUnary((UnaryNode) node, out);
    } else if (node instanceof BinaryNode && ((BinaryNode) node).op == REAL_DIV) {
      // "a ÷ b" is "(double) a / (double) b"
      BinaryNode division = (BinaryNode) node;
      add(division.left, out);
      out.add(new CastOperator(Type.DOUBLE));
      add(division.right, out);
      out.add(new CastOperator(Type.DOUBLE));
      out.add(op(DIV));
    } else if (node instanceof BinaryNode) {
      BinaryNode binary = (BinaryNode) node;
      add(binary.left, out);
      add(binary.right, out);
      out.add(op(binary.op));
    } else if (node instanceof TernaryNode) {
      TernaryNode ternary = (TernaryNode) node;
      Expression condition = new Expression(tokens(ternary.condition));
      out.add(new TernaryExpression(condition, lower(ternary.ifTrue), lower(ternary.ifFalse)));
    } else if (node instanceof InstanceOfNode) {
      InstanceOfNode test = (InstanceOfNode) node;
      add(test.operand, out);
      out.add(new InstanceofToken(test.type));
    } else if (node instanceof CastNode) {
      CastNode cast = (CastNode) node;
      if (TypeUtils.isPrimitive(cast.type)) {
        add(cast.operand, out);
        out.add(new CastOperator(cast.type));
      } else {
        // (like a parenthesized receiver without selectors)
        out.add(new CastMethodCall(cast.type, new Expression(tokens(cast.operand))));
      }
    } else if (node instanceof OpaqueNode) {
      out.addAll(((OpaqueNode) node).expr.tokens);
    } else {
      addChain(node, out);
    }
  }

  private void addUnary(UnaryNode unary, List<Token> out) {
    if (unary.op == MINUS) {
      if (unary.operand instanceof LiteralNode && isNumber(((LiteralNode) unary.operand).value)) {
        out.add(negate(((LiteralNode) unary.operand).value));
      } else {
        out.add(integer(-1));
        add(unary.operand, out);
        out.add(op(TIMES));
      }
    } else if (unary.op == BITWISE_NOT) {
      out.add(integer(-1));
      add(unary.operand, out);
      out.add(op(BITWISE_XOR));
    } else {
      add(unary.operand, out);
      out.add(op(unary.op));
    }
  }

  private static boolean isNumber(Token token) {
    return token instanceof IntLiteral || token instanceof FPLiteral;
  }

  /** A negated number literal, e.g. -1 for 1. */
  public static Token negate(Token number) {
    if (number instanceof IntLiteral) {
      IntLiteral value = (IntLiteral) number;
      BigInteger negated = ((BigInteger) value.val).negate();
      return new IntLiteral(negated, value.isLong);
    }
    FPLiteral value = (FPLiteral) number;
    BigDecimal negated = ((BigDecimal) value.val).negate();
    return new FPLiteral(negated, value.isFloat);
  }

  // A chain of selectors, e.g. "a.b[i].f(x)", becomes one Identifier token, linked by "next".
  private void addChain(ExprNode node, List<Token> out) {
    // the selectors after the first link, in order
    List<ExprNode> selectors = new ArrayList<>();
    ExprNode first = node;
    while (true) {
      if (first instanceof SelectNode) {
        selectors.add(0, first);
        first = ((SelectNode) first).target;
      } else if (first instanceof CallNode && ((CallNode) first).target != null) {
        selectors.add(0, first);
        first = ((CallNode) first).target;
      } else if (first instanceof IndexNode) {
        selectors.add(0, first);
        first = ((IndexNode) first).target;
      } else {
        break;
      }
    }
    List<Identifier> links = new ArrayList<>();
    int[] kinds = new int[selectors.size() + 1]; // (the kind of each link)
    boolean literalReceiver = false;
    if (first instanceof NameNode) {
      NameNode name = (NameNode) first;
      links.add(new Identifier(name.name));
      kinds[links.size() - 1] = name.typeName ? TYPE_NAME : NAME;
    } else if (first instanceof CallNode) {
      CallNode call = (CallNode) first;
      links.add(new MethodCall(call.name, lower(call.args)));
      kinds[links.size() - 1] = OTHER;
    } else if (first instanceof NewNode) {
      NewNode creation = (NewNode) first;
      ConstructorCall constructorCall = new ConstructorCall(creation.type, lower(creation.args));
      constructorCall.anonymousBody = creation.body;
      constructorCall.outer = (creation.outer != null) ? lower(creation.outer) : null;
      links.add(constructorCall);
      kinds[links.size() - 1] = OTHER;
    } else if (first instanceof LiteralNode && ((LiteralNode) first).value instanceof Str) {
      // e.g. "\"s\".length()": the literal, then the call chain on it
      out.add(((LiteralNode) first).value);
      literalReceiver = true;
    } else if (first instanceof CastNode && !TypeUtils.isPrimitive(((CastNode) first).type)) {
      CastNode cast = (CastNode) first;
      links.add(new CastMethodCall(cast.type, new Expression(tokens(cast.operand))));
      kinds[links.size() - 1] = OTHER;
    } else {
      // a parenthesized receiver, e.g. "(a + b)" in "(a + b).length()"
      links.add(new CastMethodCall(null, new Expression(tokens(first))));
      kinds[links.size() - 1] = OTHER;
    }
    for (ExprNode selector : selectors) {
      if (selector instanceof IndexNode) {
        // consecutive indexes are one ArraySelector, e.g. "a[i][j]"
        if (links.isEmpty()) {
          continue; // (an index of a literal: not supported)
        }
        Identifier last = links.get(links.size() - 1);
        if (last.arrSel == null) {
          last.arrSel = new ArraySelector(new ArrayList<>());
        }
        last.arrSel.exprs.add(lower(((IndexNode) selector).index));
      } else if (selector instanceof SelectNode) {
        SelectNode select = (SelectNode) selector;
        links.add(new Identifier(select.name));
        if (select.typeName) {
          kinds[links.size() - 1] = TYPE_NAME;
        } else if (select.name.equals("class") || select.name.equals("this")) {
          kinds[links.size() - 1] = OTHER;
        } else {
          kinds[links.size() - 1] = NAME;
        }
      } else {
        CallNode call = (CallNode) selector;
        links.add(new MethodCall(call.name, lower(call.args)));
        kinds[links.size() - 1] = CALL;
      }
    }
    List<Identifier> chain = mergeCallNames(links, kinds);
    if (chain.isEmpty()) {
      return;
    }
    for (int i = 0; i + 1 < chain.size(); i++) {
      chain.get(i).next = chain.get(i + 1);
    }
    Identifier root = chain.get(0);
    if (literalReceiver && root instanceof MethodCall) {
      ((MethodCall) root).literalReceiver = true;
    }
    out.add(root);
  }

  // A call takes the plain names before it into its name, e.g. "a.b.f(x)" is one MethodCall
  // "a.b.f" (but a call on an array element is not, e.g. "a[0].f(x)" is "a[0]" and "f(x)"). The
  // names start with the name of a variable, field or class, not of a built-in type, e.g. "String"
  // in "String.valueOf(1)".
  private static List<Identifier> mergeCallNames(List<Identifier> links, int[] kinds) {
    List<Identifier> chain = new ArrayList<>();
    int i = 0;
    while (i < links.size()) {
      int end = i;
      while (end < links.size()
          && (kinds[end] == NAME || (end > i && kinds[end] == TYPE_NAME))
          && links.get(end).arrSel == null) {
        end++;
      }
      if (end > i && end < links.size() && kinds[end] == CALL) {
        MethodCall call = (MethodCall) links.get(end);
        StringBuilder name = new StringBuilder();
        for (int k = i; k < end; k++) {
          name.append(links.get(k).id()).append('.');
        }
        name.append(call.name);
        MethodCall merged = new MethodCall(name.toString(), call.params);
        merged.arrSel = call.arrSel;
        chain.add(merged);
        i = end + 1;
      } else {
        chain.add(links.get(i));
        i++;
      }
    }
    return chain;
  }
}
