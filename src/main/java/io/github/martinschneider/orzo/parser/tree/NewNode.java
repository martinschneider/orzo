package io.github.martinschneider.orzo.parser.tree;

import io.github.martinschneider.orzo.parser.productions.ClassMember;
import java.util.List;

/** An object creation, e.g. "new Foo(x)" (an array creation is an OpaqueNode). */
public class NewNode extends ExprNode {
  public final String type;
  public final List<ExprNode> args;
  // the members of an anonymous class, e.g. "new Runnable() { ... }" (null for none)
  public List<ClassMember> body;
  // the type arguments, e.g. "<java.lang.String>" (null for none or "<>")
  public String typeArgs;
  // the enclosing instance of an inner class, e.g. "outer" in "outer.new Inner()" (null for none)
  public ExprNode outer;

  public NewNode(String type, List<ExprNode> args) {
    this.type = type;
    this.args = args;
  }

  @Override
  public String toString() {
    return "new " + type + ExprNode.arguments(args);
  }
}
