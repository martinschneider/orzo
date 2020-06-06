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
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("import ");
    if (isStatic) {
      strBuilder.append("static ");
    }
    strBuilder.append(id);
    return strBuilder.toString();
  }
}
