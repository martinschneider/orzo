package io.github.martinschneider.orzo.codegen;

public class ExpressionResult {
  private String type;
  private Object result;

  public ExpressionResult(String type, Object result) {
    this.type = type;
    this.result = result;
  }

  public String getType() {
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
