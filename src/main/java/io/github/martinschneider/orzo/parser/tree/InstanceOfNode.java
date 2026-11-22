package io.github.martinschneider.orzo.parser.tree;

/** A type test, e.g. "o instanceof String". */
public class InstanceOfNode extends ExprNode {
  public final ExprNode operand;
  public final String type;

  public InstanceOfNode(ExprNode operand, String type) {
    this.operand = operand;
    this.type = type;
  }

  @Override
  public String toString() {
    return "(" + operand + " instanceof " + type + ")";
  }
}
