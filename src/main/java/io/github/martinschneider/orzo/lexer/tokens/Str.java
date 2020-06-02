package io.github.martinschneider.orzo.lexer.tokens;

public class Str extends Token {
  public Str(final String value) {
    super(value);
  }

  public Str wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public String toString() {
    return "STR(" + getValue() + ")";
  }

  public Object strValue() {
    return getValue().toString();
  }
}
