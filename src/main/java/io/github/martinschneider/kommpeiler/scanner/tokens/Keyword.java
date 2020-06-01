package io.github.martinschneider.kommpeiler.scanner.tokens;

public class Keyword extends Token {
  public Keyword(Keywords value) {
    super(value);
  }

  public Keyword wLoc(Location loc) {
    this.loc = loc;
    return this;
  }
}
