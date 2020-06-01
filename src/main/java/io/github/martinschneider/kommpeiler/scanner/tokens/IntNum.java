package io.github.martinschneider.kommpeiler.scanner.tokens;

import java.math.BigInteger;

public class IntNum extends Token implements Num {
  private boolean isLong;

  public IntNum(BigInteger value, boolean isLong) {
    super(value);
    this.isLong = isLong;
  }

  public IntNum(Integer value, boolean isLong) {
    this(BigInteger.valueOf(value), isLong);
  }

  public IntNum wLoc(Location loc) {
    this.loc = loc;
    return this;
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
