package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * IntFactor
 *
 * @author Martin Schneider
 */
public class StringFactor implements Factor {
  private String value;

  /** empty constructor */
  public StringFactor() {}

  /** @param value value */
  public StringFactor(final String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return value;
  }
}
