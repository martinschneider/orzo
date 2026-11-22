package io.github.martinschneider.orzo.parser.tree;

/**
 * A simple name, e.g. "x", "this" or "Math", or the name of a built-in type (typeName: e.g. the
 * "String" in "String.valueOf(1)", or "int[]" in "int[].class").
 */
public class NameNode extends ExprNode {
  public final String name;
  public final boolean typeName;

  public NameNode(String name, boolean typeName) {
    this.name = name;
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    return name;
  }
}
