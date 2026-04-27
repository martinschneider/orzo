package io.github.martinschneider.orzo.lexer.tokens;

public class Str extends Token {
  public Str(String val) {
    super(val);
  }

  @Override
  public String toString() {
    return "STR(" + val + ")";
  }

  public String strValue() {
    return val.toString();
  }
}
