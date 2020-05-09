package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

public class Argument {
  private Identifier name;
  private String type;

  public Argument(String type, Identifier name) {
    super();
    this.name = name;
    this.type = type;
  }

  public Identifier getName() {
    return name;
  }

  public void setName(Identifier name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(type);
    strBuilder.append(' ');
    strBuilder.append(name);
    return strBuilder.toString();
  }
}
