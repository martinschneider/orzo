package io.github.martinschneider.orzo.parser.tree;

/** A type test, e.g. "o instanceof String" or the type pattern "o instanceof String s". */
public class InstanceOfNode extends ExprNode {
  public final ExprNode operand;
  public final String type;
  // the pattern variable, e.g. "s" in "o instanceof String s" (null for none)
  public String binding;

  public InstanceOfNode(ExprNode operand, String type) {
    this.operand = operand;
    this.type = type;
  }

  @Override
  public String toString() {
    return "(" + operand + " instanceof " + type + ((binding != null) ? " " + binding : "") + ")";
  }
}
