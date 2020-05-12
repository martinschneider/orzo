package io.github.martinschneider.kommpeiler.scanner.tokens;

/**
 * DoubleNum
 *
 * @author Martin Schneider
 */
public class DoubleNum extends Token implements Num {
  /** @param value value */
  public DoubleNum(final Double value) {
    super(value);
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return "DOUBLE(" + getValue() + ")";
  }

  @Override
  public void changeSign() {
    setValue((Double) getValue() * -1);
  }
}
