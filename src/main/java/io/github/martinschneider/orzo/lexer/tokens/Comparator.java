package io.github.martinschneider.orzo.lexer.tokens;

public class Comparator extends Sym {
  public Comparator(final Comparators value) {
    super(value);
  }

  public Comparator wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  public Comparators cmpValue() {
    return (Comparators) getValue();
  }
}
