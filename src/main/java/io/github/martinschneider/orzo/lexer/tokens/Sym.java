package io.github.martinschneider.orzo.lexer.tokens;

public class Sym extends Token {
  public Sym(final Object value) {
    super(value);
  }

  public Sym wLoc(Location loc) {
    this.loc = loc;
    return this;
  }
}
