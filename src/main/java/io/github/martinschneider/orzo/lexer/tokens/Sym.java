package io.github.martinschneider.orzo.lexer.tokens;

public class Sym extends Token {
  public Sym(Object val) {
    super(val);
  }

  public Sym wLoc(Location locArg) {
    this.loc = locArg;
    return this;
  }
}
