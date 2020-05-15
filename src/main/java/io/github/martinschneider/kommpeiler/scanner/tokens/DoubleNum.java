package io.github.martinschneider.kommpeiler.scanner.tokens;

public class DoubleNum extends Token implements Num {
  public DoubleNum(final Double value) {
    super(value);
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
