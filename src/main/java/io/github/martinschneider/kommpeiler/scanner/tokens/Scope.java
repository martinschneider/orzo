package io.github.martinschneider.kommpeiler.scanner.tokens;

public class Scope extends Token {
  public Scope(final Scopes value) {
    super(value);
  }

  public Scope wLoc(Location loc) {
    this.loc = loc;
    return this;
  }
}
