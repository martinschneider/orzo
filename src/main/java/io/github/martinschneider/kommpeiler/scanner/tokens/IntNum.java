package io.github.martinschneider.kommpeiler.scanner.tokens;

import java.math.BigInteger;

public class IntNum extends Token implements Num {
  public IntNum(final BigInteger value) {
    super(value);
  }

  public IntNum(final Integer value) {
    super(BigInteger.valueOf(value));
  }

  public long intValue() {
    return ((BigInteger) getValue()).longValue();
  }

  @Override
  public String toString() {
    return "INT(" + getValue() + ")";
  }

  @Override
  public void changeSign() {
    setValue(((BigInteger) getValue()).multiply(BigInteger.valueOf(-1)));
  }
}
