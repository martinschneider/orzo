package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;

/**
 * SimpleExpression simpleExpression = ["+"|"-"] term {("+"|"-") term}.
 *
 * @author Martin Schneider
 */
public class SimpleExpression extends Term {

  private Object simpleExpressionValue = null;

  private ValueType valueType = ValueType.UNKNOWN;

  /**
   * @param left left side of expression
   * @param operator operator
   * @param right right side of expression
   */
  public SimpleExpression(final Factor left, final Token operator, final Factor right) {
    super(left, operator, right);
  }

  /** empty constructor */
  public SimpleExpression() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public Object getValue() {
    this.getLeft().getValue();
    this.getRight().getValue();
    if (simpleExpressionValue != null) {
      return simpleExpressionValue;
    } else if (this.getLeft().getValueType().equals(ValueType.IMMEDIATE)
        && this.getRight().getValueType().equals(ValueType.IMMEDIATE)) {
      int value;
      if (this.getOperator().getValue().equals("PLUS")) {
        value =
            ((IntFactor) this.getLeft()).getIntValue()
                + ((IntFactor) this.getRight()).getIntValue();
        this.setValue(value);
        this.setValueType(ValueType.IMMEDIATE);
        return value;
      } else if (this.getOperator().getValue().equals("MINUS")) {
        value =
            ((IntFactor) this.getLeft()).getIntValue()
                - ((IntFactor) this.getRight()).getIntValue();
        this.setValue(value);
        return value;
      }
    }
    // else
    return null;
  }

  @Override
  public void setValue(final Object value) {
    simpleExpressionValue = value;
  }

  @Override
  public void setValueType(final ValueType valueType) {
    this.valueType = valueType;
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  /**
   * Sets the value on the left side of the operator (and resets the expression's value)
   *
   * @param left left
   */
  @Override
  public void setLeft(final Factor left) {
    super.setLeft(left);
    simpleExpressionValue = null;
  }

  /**
   * Sets the value on the right side of the operator (and resets the expression's value)
   *
   * @param right right
   */
  @Override
  public void setRight(final Factor right) {
    super.setRight(right);
    simpleExpressionValue = null;
  }

  /**
   * Sets the operator (and resets the expression's value)
   *
   * @param operator operator
   */
  public void setOperator(final Operator operator) {
    super.setOperator(operator);
    simpleExpressionValue = null;
  }
}
