package io.github.martinschneider.orzo.parser.tree;

/** An array access, e.g. "a[i]". */
public class IndexNode extends ExprNode {
  public final ExprNode target;
  public final ExprNode index;

  public IndexNode(ExprNode target, ExprNode index) {
    this.target = target;
    this.index = index;
  }

  @Override
  public String toString() {
    return target + "[" + index + "]";
  }
}
