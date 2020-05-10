package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Token;

/**
 * Term term = factor {("*" | "/" | "%") factor}
 *
 * @author Martin Schneider
 */
public class Term extends IntFactor {
  private Factor left;
  private Token operator;
  private Factor right;
  private Object termValue;

  /** empty constructor */
  public Term() {}

  /**
   * @param left left side of term
   * @param operator operator
   * @param right right side of term
   */
  public Term(final Factor left, final Token operator, final Factor right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
    getValue();
  }

  public Factor getLeft() {
    return left;
  }

  public Token getOperator() {
    return operator;
  }

  public Factor getRight() {
    return right;
  }

  /** {@inheritDoc} */
  @Override
  public Object getValue() {
    if (this.getLeft() instanceof ExpressionFactor) {
      this.getLeft().getValue();
    }
    if (this.getRight() instanceof ExpressionFactor) {
      this.getRight().getValue();
    }
    if (termValue != null) {
      return termValue;
    } else {
      int value = -1;
      if (this.getOperator().getValue().equals("TIMES")) {
        if (this.getLeft() instanceof IntFactor) {
          value =
              ((IntFactor) this.getLeft()).getIntValue()
                  * ((IntFactor) this.getRight()).getIntValue();
        } else if (this.getLeft() instanceof ExpressionFactor) {
          value =
              ((ExpressionFactor) this.getLeft()).getIntValue()
                  * ((IntFactor) this.getRight()).getIntValue();
        }
        this.setValue(value);
        return value;
      } else if (this.getOperator().getValue().equals("DIV")) {
        value =
            ((IntFactor) this.getLeft()).getIntValue()
                / ((IntFactor) this.getRight()).getIntValue();
        this.setValue(value);
        return value;
      } else if (this.getOperator().getValue().equals("MOD")) {
        value =
            ((IntFactor) this.getLeft()).getIntValue()
                % ((IntFactor) this.getRight()).getIntValue();
        this.setValue(value);
        return value;
      }
    }
    return null;
  }

  /**
   * Sets the value on the left side of the operator (and resets the term's value)
   *
   * @param left left
   */
  public void setLeft(final Factor left) {
    this.left = left;
    termValue = null;
  }

  /**
   * Sets the operator (and resets the value)
   *
   * @param operator operator
   */
  public void setOperator(final Token operator) {
    this.operator = operator;
    termValue = null;
  }

  /**
   * Sets the value on the right side of the operator (and resets the term's value)
   *
   * @param right right
   */
  public void setRight(final Factor right) {
    this.right = right;
    termValue = null;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return left.toString() + " " + operator.toString() + " " + right.toString();
  }
}
