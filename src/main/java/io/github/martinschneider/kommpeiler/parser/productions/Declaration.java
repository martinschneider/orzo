package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

public class Declaration implements Statement {
  private boolean hasValue;
  private Identifier name;
  private String type;
  private Expression value;

  public Declaration(
      final Identifier name, final String type, final Expression value, final boolean hasValue) {
    super();
    this.type = type;
    this.name = name;
    this.value = value;
    this.hasValue = hasValue;
  }

  public Identifier getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Expression getValue() {
    return value;
  }

  public boolean hasValue() {
    return hasValue;
  }

  public void setHasValue(final boolean hasValue) {
    this.hasValue = hasValue;
  }

  public void setName(final Identifier name) {
    this.name = name;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setValue(final Expression value) {
    this.value = value;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(type.toString());
    strBuilder.append(' ');
    strBuilder.append(name);
    strBuilder.append('=');
    strBuilder.append(value);
    return strBuilder.toString();
  }
}
