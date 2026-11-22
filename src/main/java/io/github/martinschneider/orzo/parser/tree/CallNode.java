package io.github.martinschneider.orzo.parser.tree;

import java.util.List;

/** A method call, e.g. "f(x)" (without a target) or "a.f(x)". */
public class CallNode extends ExprNode {
  public final ExprNode target;
  public final String name;
  public final List<ExprNode> args;

  public CallNode(ExprNode target, String name, List<ExprNode> args) {
    this.target = target;
    this.name = name;
    this.args = args;
  }

  @Override
  public String toString() {
    String call = name + ExprNode.arguments(args);
    if (target == null) {
      return call;
    }
    return target + "." + call;
  }
}
