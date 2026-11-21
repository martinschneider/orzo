package io.github.martinschneider.orzo.lexer.tokens;

import java.util.Objects;

/**
 * A cast to a primitive type inside an expression, e.g. the "(int)" in "a + (int) b": a prefix
 * unary operator in the parsed (postfix) expression, so it follows its operand ("a b CAST(int)
 * PLUS").
 */
public class CastOperator extends Operator {
  public final String type;

  public CastOperator(String type) {
    super(Operators.CAST);
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof CastOperator && type.equals(((CastOperator) obj).type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(val, type);
  }

  @Override
  public String toString() {
    return "CAST(" + type + ")";
  }
}
