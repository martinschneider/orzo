package io.github.martinschneider.kommpeiler.parser.productions;

public class Import {
  public Import(String identifier, boolean isStatic) {
    super();
    this.identifier = identifier;
    this.isStatic = isStatic;
  }

  private String identifier;
  private boolean isStatic;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("import ");
    if (isStatic) {
      strBuilder.append("static ");
    }
    strBuilder.append(identifier);
    return strBuilder.toString();
  }
}
