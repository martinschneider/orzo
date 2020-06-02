package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;

public class FieldSelector extends Selector {
  private Identifier identifier;

  public void setIdentifier(final Identifier identifier) {
    this.identifier = identifier;
  }

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
