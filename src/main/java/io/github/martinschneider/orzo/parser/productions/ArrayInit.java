package io.github.martinschneider.orzo.parser.productions;

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
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((dims == null) ? 0 : dims.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((vals == null) ? 0 : vals.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    ArrayInit other = (ArrayInit) obj;
    if (dims == null) {
      if (other.dims != null) return false;
    } else if (!dims.equals(other.dims)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    if (vals == null) {
      if (other.vals != null) return false;
    } else if (!vals.equals(other.vals)) return false;
    return true;
  }

  @Override
  public String toString() {
    return type;
  }

  public String typeDescr() {
    String result = type;
    for (int i = 0; i < dims.size(); i++) {
      result = "[" + result;
    }
    return result;
  }
}
