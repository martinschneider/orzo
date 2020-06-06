package io.github.martinschneider.orzo.lexer.tokens;

public class Scope extends Token {
  public Scope(Scopes val) {
    super(val);
  }

  public Scope wLoc(Location loc) {
    this.loc = loc;
    return this;
  }
}
