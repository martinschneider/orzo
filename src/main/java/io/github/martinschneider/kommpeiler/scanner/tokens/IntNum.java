package io.github.martinschneider.kommpeiler.scanner.tokens;

public class IntNum extends Token implements Num {
  public IntNum(final Integer value) {
    super(value);
  }

  public int intValue() {
    return (Integer) getValue();
  }

  @Override
  public String toString() {
    return "INT(" + getValue() + ")";
  }

  @Override
  public void changeSign() {
    setValue((Integer) getValue() * -1);
  }
}