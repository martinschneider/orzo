package io.github.martinschneider.kommpeiler.scanner.tokens;

public class Operator extends Sym {
  public Operator(final Operators value) {
    super(value);
  }

  public Operators opValue() {
    return ((Operators) getValue());
  }

  public int precedence() {
    return ((Operators) getValue()).getPrecedence();
  }
}
