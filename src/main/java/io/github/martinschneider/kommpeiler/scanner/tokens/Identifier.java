package io.github.martinschneider.kommpeiler.scanner.tokens;

import io.github.martinschneider.kommpeiler.parser.productions.Selector;

/**
 * Identifier
 *
 * @author Martin Schneider
 */
public class Identifier extends Token {
  private Selector selector;

  /** @param value value */
  public Identifier(final String value) {
    super(value);
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return getValue();
  }

  public void setSelector(final Selector selector) {
    this.selector = selector;
  }

  public Selector getSelector() {
    return selector;
  }
}
