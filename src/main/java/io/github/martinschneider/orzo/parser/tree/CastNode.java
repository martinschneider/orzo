package io.github.martinschneider.orzo.parser.tree;

/** A cast, e.g. "(int) x" or "(java.lang.String) o". */
public class CastNode extends ExprNode {
  public final String type;
  public final ExprNode operand;

  public CastNode(String type, ExprNode operand) {
    this.type = type;
    this.operand = operand;
  }

  @Override
  public String toString() {
    return "((" + type + ") " + operand + ")";
  }
}
