package io.github.martinschneider.kommpeiler.scanner.tokens;

/**
 * IntNum
 *
 * @author Martin Schneider
 */
public class IntNum extends Token implements Num {
  /** @param value value */
  public IntNum(final Integer value) {
    super(value);
  }

  public int intValue() {
    return (Integer) getValue();
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return "INT(" + getValue() + ")";
  }

  @Override
  public void changeSign() {
    setValue((Integer) getValue() * -1);
  }
}
