package io.github.martinschneider.orzo.lexer.tokens;

public class Operator extends Sym {
  public Operator(final Operators value) {
    super(value);
  }

  public Operator wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  public Operators opValue() {
    return ((Operators) getValue());
  }

  public int precedence() {
    return ((Operators) getValue()).getPrecedence();
  }
}
