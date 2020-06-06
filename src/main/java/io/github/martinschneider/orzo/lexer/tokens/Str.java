package io.github.martinschneider.orzo.lexer.tokens;

public class Str extends Token {
  public Str(String val) {
    super(val);
  }

  public Str wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public String toString() {
    return "STR(" + val + ")";
  }

  public Object strValue() {
    return val.toString();
  }
}
