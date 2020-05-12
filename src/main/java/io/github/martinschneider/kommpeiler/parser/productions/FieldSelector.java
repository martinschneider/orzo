package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

/**
 * FieldSelector
 *
 * @author Martin Schneider
 */
public class FieldSelector extends Selector {
  private Identifier identifier;

  public void setIdentifier(final Identifier identifier) {
    this.identifier = identifier;
  }

  /** @param identifier identifier */
  public FieldSelector(final Identifier identifier) {
    super();
    this.identifier = identifier;
  }

  public Identifier getIdentifier() {
    return identifier;
  }

  @Override
  public String toString() {
    return identifier.toString();
  }
}
