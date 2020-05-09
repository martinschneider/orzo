package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * ExpressionFactor
 *
 * @author Martin Schneider
 */
public class ExpressionFactor implements Factor {
  private ValueType valueType = ValueType.UNKNOWN;

  private Factor expressionValue;

  public Factor getExpressionValue() {
    return expressionValue;
  }

  @Override
  public Object getValue() {
    this.valueType = expressionValue.getValueType();
    return expressionValue.getValue();
  }

  public int getIntValue() {
    return (Integer) expressionValue.getValue();
  }

  public void setValue(final Factor value) {
    this.expressionValue = value;
  }

  /** empty constructor */
  public ExpressionFactor() {}

  /** @param value value */
  public ExpressionFactor(final Factor value) {
    this.expressionValue = value;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return expressionValue.toString();
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
