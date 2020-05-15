package io.github.martinschneider.kommpeiler.scanner.tokens;

public enum Operators {
  ASSIGN(1),
  MINUS(11),
  PLUS(11),
  TIMES(12),
  DIV(12),
  MOD(12),
  PRE_INCREMENT(14),
  PRE_DECREMENT(14),
  POST_INCREMENT(15),
  POST_DECREMENT(15);
  final int precedence;

  private Operators(int precedence) {
    this.precedence = precedence;
  }

  public int getPrecedence() {
    return precedence;
  }
}
