package io.github.martinschneider.orzo.parser.tree;

/**
 * A member selection, e.g. "a.b", "Foo.class" or (typeName) the last segment of a qualified name
 * that is the name of a built-in type, e.g. "String" in "java.lang.String".
 */
public class SelectNode extends ExprNode {
  public final ExprNode target;
  public final String name;
  public final boolean typeName;

  public SelectNode(ExprNode target, String name, boolean typeName) {
    this.target = target;
    this.name = name;
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    return target + "." + name;
  }
}
