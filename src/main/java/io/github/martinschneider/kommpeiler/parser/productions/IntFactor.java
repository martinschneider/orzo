package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * IntFactor
 *
 * @author Martin Schneider
 */
public class IntFactor implements Factor {
  private int value;

  private ValueType valueType = ValueType.IMMEDIATE;

  @Override
  public Object getValue() {
    return value;
  }

  public int getIntValue() {
    return value;
  }

  public void setValue(final Object value) {
    this.value = ((Integer) value).intValue();
  }

  /** empty constructor */
  public IntFactor() {}

  /** @param value value */
  public IntFactor(final int value) {
    this.value = value;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return Integer.toString(value);
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
