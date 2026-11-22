package io.github.martinschneider.orzo.parser.tree;

import io.github.martinschneider.orzo.parser.productions.Expression;

/**
 * An expression the tree doesn't model (yet), already in the postfix form of the code generator: an
 * array creation (an ArrayInit), a lambda or a method reference. Also a placeholder for a missing
 * operand after a syntax error (an empty expression).
 */
public class OpaqueNode extends ExprNode {
  public final Expression expr;

  public OpaqueNode(Expression expr) {
    this.expr = expr;
  }

  @Override
  public String toString() {
    return expr.tokens.toString();
  }
}
