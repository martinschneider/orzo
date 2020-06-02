package io.github.martinschneider.orzo.lexer.tokens;

public class Scope extends Token {
  public Scope(final Scopes value) {
    super(value);
  }

  public Scope wLoc(Location loc) {
    this.loc = loc;
    return this;
  }
}
