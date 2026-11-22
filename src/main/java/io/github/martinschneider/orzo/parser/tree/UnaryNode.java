package io.github.martinschneider.orzo.parser.tree;

import io.github.martinschneider.orzo.lexer.tokens.Operators;

/**
 * A unary operation: a negation "-x" (MINUS), "!x" (NEGATE), "~x" (BITWISE_NOT) or an increment or
 * decrement ("++x", "x++", ...).
 */
public class UnaryNode extends ExprNode {
  public final Operators op;
  public final ExprNode operand;

  public UnaryNode(Operators op, ExprNode operand) {
    this.op = op;
    this.operand = operand;
  }

  @Override
  public String toString() {
    return op + "(" + operand + ")";
  }
}
