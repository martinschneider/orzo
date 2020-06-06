package io.github.martinschneider.orzo.codegen;

public class ExpressionResult {
  public String type;
  public Object result;

  public ExpressionResult(String type, Object result) {
    this.type = type;
    this.result = result;
  }

  @Override
  public String toString() {
    return "[" + type + " " + result + "]";
  }
}
