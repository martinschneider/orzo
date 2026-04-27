package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class ArrayInit extends Expression {
  public String type;
  public List<Expression> dims;
  public List<List<Expression>> vals;

  public ArrayInit(String type, List<Expression> dims, List<List<Expression>> vals) {
    this.type = type;
    this.dims = dims;
    this.vals = vals;
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

  public String typeDescr() {
    String result = type;
    for (int i = 0; i < dims.size(); i++) {
      result = "[" + result;
    }
    return result;
  }
}
