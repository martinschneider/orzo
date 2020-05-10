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
  private ExpressionType type;

  /** empty constructor */
  public SimpleExpression() {
    super();
  }

  /**
   * @param left left side of expression
   * @param operator operator
   * @param right right side of expression
   */
  public SimpleExpression(final Factor left, final Token operator, final Factor right) {
    super(left, operator, right);
    if (this.getLeft() instanceof IntFactor && this.getRight() instanceof IntFactor) {
      setType(ExpressionType.VALUE);
      int value;
      if (this.getOperator().getValue().equals("PLUS")) {
        value =
            ((IntFactor) this.getLeft()).getIntValue()
                + ((IntFactor) this.getRight()).getIntValue();
        this.setValue(value);
      } else if (this.getOperator().getValue().equals("MINUS")) {
        value =
            ((IntFactor) this.getLeft()).getIntValue()
                - ((IntFactor) this.getRight()).getIntValue();
        this.setValue(value);
      }
    } else if (this.getLeft() instanceof IdFactor && this.getRight() instanceof IdFactor) {
      setType(ExpressionType.VARIABLE_AND_VARIABLE);
    }
  }

  public ExpressionType getType() {
    return type;
  }

  /** {@inheritDoc} */
  @Override
  public Object getValue() {
    this.getLeft().getValue();
    this.getRight().getValue();
    if (simpleExpressionValue != null) {
      return simpleExpressionValue;
    }
    // else
    return null;
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
   * Sets the operator (and resets the expression's value)
   *
   * @param operator operator
   */
  public void setOperator(final Operator operator) {
    super.setOperator(operator);
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

  public void setType(ExpressionType type) {
    this.type = type;
  }

  @Override
  public void setValue(final Object value) {
    simpleExpressionValue = value;
  }
}
