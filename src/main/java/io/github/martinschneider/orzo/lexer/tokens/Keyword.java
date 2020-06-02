package io.github.martinschneider.orzo.lexer.tokens;

public class Keyword extends Token {
  public Keyword(Keywords value) {
    super(value);
  }

  public Keyword wLoc(Location loc) {
    this.loc = loc;
    return this;
  }
}
