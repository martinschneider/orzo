package io.github.martinschneider.orzo.lexer.tokens;

public class Operator extends Sym {
  public Operator(Operators val) {
    super(val);
  }

  public Operators opValue() {
    return ((Operators) val);
  }

  public int precedence() {
    return ((Operators) val).getPrecedence();
  }
}
