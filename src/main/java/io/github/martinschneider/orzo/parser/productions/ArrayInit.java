package io.github.martinschneider.orzo.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

public class ArrayInit extends Expression {
  public String type;
  public List<Integer> dims;
  public List<List<Expression>> vals;

  public ArrayInit(String type, List<Integer> dims, List<List<Expression>> vals) {
    this.type = type;
    this.dims = dims;
    this.vals = vals;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((dims == null) ? 0 : dims.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((vals == null) ? 0 : vals.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ArrayInit other = (ArrayInit) obj;
    if (dims == null) {
      if (other.dims != null) {
        return false;
      }
    } else if (!dims.equals(other.dims)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    if (vals == null) {
      if (other.vals != null) {
        return false;
      }
    } else if (!vals.equals(other.vals)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("new ");
    strBuilder.append(type);
    for (Integer dim : dims) {
      strBuilder.append('[');
      strBuilder.append(dim);
      strBuilder.append(']');
    }
    strBuilder.append('{');
    strBuilder.append(vals.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append('}');
    return strBuilder.toString();
  }
}
