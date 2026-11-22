package io.github.martinschneider.orzo.parser.tree;

/** A conditional expression "a ? b : c". */
public class TernaryNode extends ExprNode {
  public final ExprNode condition;
  public final ExprNode ifTrue;
  public final ExprNode ifFalse;

  public TernaryNode(ExprNode condition, ExprNode ifTrue, ExprNode ifFalse) {
    this.condition = condition;
    this.ifTrue = ifTrue;
    this.ifFalse = ifFalse;
  }

  @Override
  public String toString() {
    return "(" + condition + " ? " + ifTrue + " : " + ifFalse + ")";
  }
}
