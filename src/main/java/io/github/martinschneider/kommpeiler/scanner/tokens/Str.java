package io.github.martinschneider.kommpeiler.scanner.tokens;

public class Str extends Token {
  public Str(final String value) {
    super(value);
  }

  @Override
  public String toString() {
    return "STR(" + getValue() + ")";
  }

  public Object strValue() {
    return getValue().toString();
  }
}
