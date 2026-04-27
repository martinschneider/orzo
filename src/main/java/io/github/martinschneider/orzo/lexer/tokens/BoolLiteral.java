package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.util.ObjectUtils;

public class BoolLiteral extends Token {
  public BoolLiteral(boolean val) {
    super(val);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
