package io.github.martinschneider.kommpeiler.scanner.tokens;

import java.math.BigInteger;

public class IntNum extends Token implements Num {
  private boolean isLong;

  public IntNum(final BigInteger value, boolean isLong) {
    super(value);
    this.isLong = isLong;
  }

  public IntNum(final Integer value, final boolean isLong) {
    this(BigInteger.valueOf(value), isLong);
  }

  public long intValue() {
    return ((BigInteger) getValue()).longValue();
  }

  public boolean isLong() {
    return isLong;
  }

  public void setLong(boolean isLong) {
    this.isLong = isLong;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("INT(");
    strBuilder.append(getValue());
    if (isLong) {
      strBuilder.append("l");
    }
    strBuilder.append(')');
    return strBuilder.toString();
  }

  @Override
  public void changeSign() {
    setValue(((BigInteger) getValue()).multiply(BigInteger.valueOf(-1)));
  }
}
