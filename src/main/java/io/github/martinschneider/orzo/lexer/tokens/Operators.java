package io.github.martinschneider.orzo.lexer.tokens;

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
  BITWISE_OR_ASSIGN(1),
  BITWISE_XOR_ASSIGN(1),
  BITWISE_AND_ASSIGN(1),
  BITWISE_OR(5),
  BITWISE_XOR(6),
  BITWISE_AND(7),
  MINUS(11),
  PLUS(11),
  TIMES(12),
  POW(10),
  DIV(12),
  MOD(12),
  LSHIFT(10),
  RSHIFT(10),
  RSHIFTU(10),
  PRE_INCREMENT(14),
  PRE_DECREMENT(14),
  POST_INCREMENT(15),
  POST_DECREMENT(15);

  int precedence;

  private Operators(int precedence) {
    this.precedence = precedence;
  }

  public int getPrecedence() {
    return precedence;
  }
}
