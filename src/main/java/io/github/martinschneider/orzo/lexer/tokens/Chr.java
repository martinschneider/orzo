package io.github.martinschneider.orzo.lexer.tokens;

public class Chr extends Token {
  public Chr(char val) {
    super(val);
  }

  public Chr wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public String toString() {
    return "CHR(" + val + ")";
  }
}
