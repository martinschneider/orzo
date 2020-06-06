package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;

public class FieldSelector extends Selector {
  public Identifier identifier;

  public FieldSelector(Identifier identifier) {
    this.identifier = identifier;
  }

  @Override
  public String toString() {
    return identifier.toString();
  }
}
