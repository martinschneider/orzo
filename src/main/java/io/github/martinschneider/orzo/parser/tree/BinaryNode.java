package io.github.martinschneider.orzo.parser.tree;

import io.github.martinschneider.orzo.lexer.tokens.Operators;

/** A binary operation, e.g. "a + b", "a && b" or an assignment "a = b". */
public class BinaryNode extends ExprNode {
  public final Operators op;
  public final ExprNode left;
  public final ExprNode right;

  public BinaryNode(Operators op, ExprNode left, ExprNode right) {
    this.op = op;
    this.left = left;
    this.right = right;
  }

  @Override
  public String toString() {
    return op + "(" + left + ", " + right + ")";
  }
}
