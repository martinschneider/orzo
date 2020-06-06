package io.github.martinschneider.orzo.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

public class ArrayInitialiser extends Expression {
  public String type;
  public int size;
  public List<Expression> vals;

  public ArrayInitialiser(String type, int size, List<Expression> vals) {
    this.type = type;
    this.size = size;
    this.vals = vals;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = super.hashCode();
    result = prime * result + size;
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
    ArrayInitialiser other = (ArrayInitialiser) obj;
    if (size != other.size) {
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
    strBuilder.append('[');
    strBuilder.append(size);
    strBuilder.append(']');
    strBuilder.append('{');
    strBuilder.append(vals.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append('}');
    return strBuilder.toString();
  }
}
