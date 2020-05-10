package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

/**
 * Declaration
 *
 * @author Martin Schneider
 */
public class Declaration extends Statement {
  private boolean hasValue;
  private Identifier name;
  private String type;
  private Factor value;

  /**
   * @param name name
   * @param type type
   * @param value value
   * @param hasValue true if value is known
   */
  public Declaration(
      final Identifier name, final String type, final Factor value, final boolean hasValue) {
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

  public Factor getValue() {
    return value;
  }

  /** @return true if value is known */
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

  public void setValue(final Factor value) {
    this.value = value;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(type);
    strBuilder.append(' ');
    strBuilder.append(name);
    strBuilder.append('=');
    strBuilder.append(value);
    return strBuilder.toString();
  }
}
