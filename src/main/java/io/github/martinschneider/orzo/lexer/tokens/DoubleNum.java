package io.github.martinschneider.orzo.lexer.tokens;

import java.math.BigDecimal;

public class DoubleNum extends Token implements Num {
  private boolean isFloat;

  public DoubleNum(BigDecimal value, boolean isFloat) {
    super(value);
    this.isFloat = isFloat;
  }

  public DoubleNum(Double value, boolean isFloat) {
    this(BigDecimal.valueOf(value), isFloat);
  }

  public DoubleNum wLoc(Location loc) {
    this.loc = loc;
    return this;
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
