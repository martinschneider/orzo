package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;

public class ThrowStatement implements Statement {
  public ConstructorCall constructorCall;
  // any other thrown expression, e.g. "throw e;" (null if constructorCall is set)
  public Expression expr;

  public ThrowStatement(ConstructorCall constructorCall) {
    this.constructorCall = constructorCall;
  }

  public ThrowStatement(Expression expr) {
    this.expr = expr;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return ObjectUtils.equals(this, obj);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
