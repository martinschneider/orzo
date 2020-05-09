package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * IntFactor
 *
 * @author Martin Schneider
 */
public class StringFactor implements Factor {
  private String value;

  private ValueType valueType = ValueType.IMMEDIATE;

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  /** empty constructor */
  public StringFactor() {}

  /** @param value value */
  public StringFactor(final String value) {
    this.value = value;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return value;
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  @Override
  public void setValueType(final ValueType valueType) {
    this.valueType = valueType;
  }
}
