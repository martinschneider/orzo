package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * ExpressionFactor
 *
 * @author Martin Schneider
 */
public class ExpressionFactor implements Factor {
  private Factor expressionValue;

  /** empty constructor */
  public ExpressionFactor() {}

  /** @param value value */
  public ExpressionFactor(final Factor value) {
    this.expressionValue = value;
  }

  public Factor getExpressionValue() {
    return expressionValue;
  }

  public int getIntValue() {
    return (Integer) expressionValue.getValue();
  }

  @Override
  public Object getValue() {
    return expressionValue;
  }

  public void setValue(final Factor value) {
    this.expressionValue = value;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return expressionValue.toString();
  }
}
