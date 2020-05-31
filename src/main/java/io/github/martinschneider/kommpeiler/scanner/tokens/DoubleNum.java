package io.github.martinschneider.kommpeiler.scanner.tokens;

import java.math.BigDecimal;

public class DoubleNum extends Token implements Num {
  private boolean isFloat;

  public DoubleNum(final BigDecimal value, final boolean isFloat) {
    super(value);
    this.isFloat = isFloat;
  }

  public DoubleNum(final Double value, final boolean isFloat) {
    this(BigDecimal.valueOf(value), isFloat);
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("FP(");
    strBuilder.append(getValue());
    if (isFloat) {
      strBuilder.append("f");
    }
    strBuilder.append(')');
    return strBuilder.toString();
  }

  @Override
  public void changeSign() {
    setValue((Double) getValue() * -1);
  }

  public boolean isFloat() {
    return isFloat;
  }

  public void setFloat(boolean isFloat) {
    this.isFloat = isFloat;
  }
}
