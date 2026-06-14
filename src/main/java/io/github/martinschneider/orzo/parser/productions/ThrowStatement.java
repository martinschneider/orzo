package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;

public class ThrowStatement implements Statement {
  public ConstructorCall constructorCall;

  public ThrowStatement(ConstructorCall constructorCall) {
    this.constructorCall = constructorCall;
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
