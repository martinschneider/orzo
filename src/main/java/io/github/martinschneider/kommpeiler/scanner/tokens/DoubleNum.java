package io.github.martinschneider.kommpeiler.scanner.tokens;

import java.math.BigDecimal;

public class DoubleNum extends Token implements Num {
  public DoubleNum(final BigDecimal value) {
    super(value);
  }

  public DoubleNum(final Double value) {
    super(BigDecimal.valueOf(value));
  }

  @Override
  public String toString() {
    return "DOUBLE(" + getValue() + ")";
  }

  @Override
  public void changeSign() {
    setValue((Double) getValue() * -1);
  }
}
