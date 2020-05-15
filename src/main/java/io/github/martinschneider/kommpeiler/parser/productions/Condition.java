package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Comparator;

public class Condition {
  private Expression left;
  private Comparator operator;
  private Expression right;

  public Condition() {}

  public Condition(final Expression left, final Comparator operator, final Expression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public Expression getLeft() {
    return left;
  }

  public Comparator getOperator() {
    return operator;
  }

  public Expression getRight() {
    return right;
  }

  public void setLeft(final Expression left) {
    this.left = left;
  }

  public void setOperator(final Comparator operator) {
    this.operator = operator;
  }

  public void setRight(final Expression right) {
    this.right = right;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((left == null) ? 0 : left.hashCode());
    result = prime * result + ((operator == null) ? 0 : operator.hashCode());
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
    if (operator == null) {
      if (other.operator != null) {
        return false;
      }
    } else if (!operator.equals(other.operator)) {
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
    return left.toString() + " " + operator.toString() + " " + right.toString();
  }
}
