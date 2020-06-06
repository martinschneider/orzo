package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class ArraySelector extends Selector {
  public List<Expression> exprs;

  public ArraySelector(List<Expression> expressions) {
    this.exprs = expressions;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((exprs == null) ? 0 : exprs.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ArraySelector other = (ArraySelector) obj;
    if (exprs == null) {
      if (other.exprs != null) {
        return false;
      }
    } else if (!exprs.equals(other.exprs)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    for (Expression expr : exprs) {
      strBuilder.append('[');
      strBuilder.append(expr);
      strBuilder.append(']');
    }
    return strBuilder.toString();
  }
}
