package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

public class Type extends Identifier {
  public Type(String value) {
    super(value.toUpperCase());
  }

  public String getLabel() {
    return BasicType.valueOf(getValue().toString()).getLabel();
  }
}
