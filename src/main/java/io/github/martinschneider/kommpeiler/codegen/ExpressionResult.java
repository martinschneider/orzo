package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.parser.productions.Type;

public class ExpressionResult {
  private Type type;
  private Object result;

  public ExpressionResult(Type type, Object result) {
    this.type = type;
    this.result = result;
  }

  public Type getType() {
    return type;
  }

  public Object getValue() {
    return result;
  }

  @Override
  public String toString() {
    return "[" + type + " " + result + "]";
  }
}
