package io.github.martinschneider.kommpeiler.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

public class ArrayInitialiser extends Expression {
  private String type;
  private int size;
  private List<Expression> values;

  public ArrayInitialiser(String type, int size, List<Expression> values) {
    super();
    this.type = type;
    this.size = size;
    this.values = values;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public List<Expression> getValues() {
    return values;
  }

  public void setValues(List<Expression> values) {
    this.values = values;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + size;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((values == null) ? 0 : values.hashCode());
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
    if (values == null) {
      if (other.values != null) {
        return false;
      }
    } else if (!values.equals(other.values)) {
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
    strBuilder.append(values.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append('}');
    return strBuilder.toString();
  }
}
