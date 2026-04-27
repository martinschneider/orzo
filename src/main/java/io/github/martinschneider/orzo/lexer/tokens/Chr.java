package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.util.ObjectUtils;

public class Chr extends Token {
  public Chr(char val) {
    super(val);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
