package io.github.martinschneider.orzo.parser.productions;

public class Import {
  public String id;
  public boolean isStatic;

  public Import(String id, boolean isStatic) {
    this.id = id;
    this.isStatic = isStatic;
  }

  @Override
  public String toString() {
    return id;
  }
}
