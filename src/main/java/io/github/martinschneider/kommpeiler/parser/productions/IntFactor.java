package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * IntFactor
 *
 * @author Martin Schneider
 */
public class IntFactor implements Factor {
  private int value;

  /** empty constructor */
  public IntFactor() {}

  /** @param value value */
  public IntFactor(final int value) {
    this.value = value;
  }

  public int getIntValue() {
    return value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  public void setValue(final Object value) {
    this.value = ((Integer) value).intValue();
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
