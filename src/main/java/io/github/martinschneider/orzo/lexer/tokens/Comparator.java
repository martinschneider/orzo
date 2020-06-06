package io.github.martinschneider.orzo.lexer.tokens;

public class Comparator extends Sym {
  public Comparator(Comparators val) {
    super(val);
  }

  @Override
  public Comparator wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  public Comparators cmpValue() {
    return (Comparators) val;
  }
}
