package io.github.martinschneider.kommpeiler.parser.productions;

import java.util.List;

public class ArraySelector extends Selector {
  public ArraySelector(List<Expression> expressions) {
    super();
    this.expressions = expressions;
  }

  private List<Expression> expressions;

  public List<Expression> getExpression() {
    return expressions;
  }

  public void setExpression(List<Expression> expression) {
    this.expressions = expression;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((expressions == null) ? 0 : expressions.hashCode());
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
    if (expressions == null) {
      if (other.expressions != null) {
        return false;
      }
    } else if (!expressions.equals(other.expressions)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    for (Expression expr : expressions) {
      strBuilder.append('[');
      strBuilder.append(expr);
      strBuilder.append(']');
    }
    return strBuilder.toString();
  }
}
