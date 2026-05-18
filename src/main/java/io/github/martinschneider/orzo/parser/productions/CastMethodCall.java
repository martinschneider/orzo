package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;

public class CastMethodCall extends Identifier {
  public String castType;
  public Expression innerExpr;

  public CastMethodCall(String castType, Expression innerExpr) {
    super("$castMethodCall", null);
    this.castType = castType;
    this.innerExpr = innerExpr;
  }
}
