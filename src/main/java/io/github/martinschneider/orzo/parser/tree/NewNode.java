package io.github.martinschneider.orzo.parser.tree;

import java.util.List;

/** An object creation, e.g. "new Foo(x)" (an array creation is an OpaqueNode). */
public class NewNode extends ExprNode {
  public final String type;
  public final List<ExprNode> args;

  public NewNode(String type, List<ExprNode> args) {
    this.type = type;
    this.args = args;
  }

  @Override
  public String toString() {
    return "new " + type + ExprNode.arguments(args);
  }
}
