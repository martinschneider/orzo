package io.github.martinschneider.orzo.parser.productions;

public class Increment implements Statement {
  public Expression expr;

  public Increment(Expression expr) {
    this.expr = expr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
    Increment other = (Increment) obj;
    if (expr == null) {
      if (other.expr != null) {
        return false;
      }
    } else if (!expr.equals(other.expr)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
