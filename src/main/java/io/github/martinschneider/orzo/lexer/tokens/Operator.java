package io.github.martinschneider.orzo.lexer.tokens;

public class Operator extends Sym {
  public Operator(Operators val) {
    super(val);
  }

  @Override
  public Operator wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  public Operators opValue() {
    return ((Operators) val);
  }

  public int precedence() {
    return ((Operators) val).getPrecedence();
  }
}
