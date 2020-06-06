package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Comparator;

public class Condition {
  public Expression left;
  public Comparator op;
  public Expression right;

  public Condition() {}

  public Condition(Expression left, Comparator operator, Expression right) {
    this.left = left;
    this.op = operator;
    this.right = right;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((left == null) ? 0 : left.hashCode());
    result = prime * result + ((op == null) ? 0 : op.hashCode());
    result = prime * result + ((right == null) ? 0 : right.hashCode());
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
    Condition other = (Condition) obj;
    if (left == null) {
      if (other.left != null) {
        return false;
      }
    } else if (!left.equals(other.left)) {
      return false;
    }
    if (op == null) {
      if (other.op != null) {
        return false;
      }
    } else if (!op.equals(other.op)) {
      return false;
    }
    if (right == null) {
      if (other.right != null) {
        return false;
      }
    } else if (!right.equals(other.right)) {
      return false;
    }
    return true;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return left.toString() + " " + op.toString() + " " + right.toString();
  }
}
