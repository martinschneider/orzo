package io.github.martinschneider.orzo.lexer.tokens;

// http://www.cs.bilkent.edu.tr/~guvenir/courses/CS101/op_precedence.html
public enum Operator {
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
  LOGICAL_OR(3),
  LOGICAL_AND(4),
  BITWISE_OR(5),
  BITWISE_XOR(6),
  BITWISE_AND(7),
  EQUAL(8),
  NOTEQUAL(8),
  GREATER(9),
  LESS(9),
  LESSEQ(9),
  GREATEREQ(9),
  MINUS(11),
  PLUS(11),
  TIMES(12),
  POW(10),
  DIV(12),
  MOD(12),
  LSHIFT(10),
  RSHIFT(10),
  RSHIFTU(10),
  NEGATE(13),
  PRE_INCREMENT(13),
  PRE_DECREMENT(13),
  POST_INCREMENT(14),
  POST_DECREMENT(14);

  int precedence;

  private Operator(int precedence) {
    this.precedence = precedence;
  }

  public int getPrecedence() {
    return precedence;
  }
}
