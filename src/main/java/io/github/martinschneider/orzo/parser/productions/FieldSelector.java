package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.util.ObjectUtils;

public class FieldSelector extends Selector {
  public Identifier identifier;

  public FieldSelector(Identifier identifier) {
    this.identifier = identifier;
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
