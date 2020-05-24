package io.github.martinschneider.kommpeiler.scanner.tokens;

public enum Operators {
  ASSIGN(1),
  PLUS_ASSIGN(1),
  MINUS_ASSIGN(1),
  TIMES_ASSIGN(1),
  DIV_ASSIGN(1),
  MOD_ASSIGN(1),
  LSHIFT_ASSIGN(1),
  RSHIFT_ASSIGN(1),
  RSHIFTU_ASSIGN(1),
  MINUS(11),
  PLUS(11),
  TIMES(12),
  DIV(12),
  MOD(12),
  LSHIFT(10),
  RSHIFT(10),
  RSHIFTU(10),
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
