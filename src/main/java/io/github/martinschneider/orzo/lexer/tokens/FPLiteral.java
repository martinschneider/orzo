package io.github.martinschneider.orzo.lexer.tokens;

import java.math.BigDecimal;

public class FPLiteral extends Token {
  public boolean isFloat;

  public FPLiteral(BigDecimal val, boolean isFloat) {
    super(val);
    this.isFloat = isFloat;
  }

  public FPLiteral(Double val, boolean isFloat) {
    this(BigDecimal.valueOf(val), isFloat);
  }

  public double doubleVal() {
    return ((BigDecimal) val).doubleValue();
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("FP(");
    strBuilder.append(val);
    if (isFloat) {
      strBuilder.append("f");
    }
    strBuilder.append(')');
    return strBuilder.toString();
  }
}
