package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

public class Assignment implements Statement {
  private Identifier left;
  private Expression right;

  public Assignment() {}

  public Assignment(final Identifier left, final Expression right) {
    this.left = left;
    this.right = right;
  }

  public Identifier getLeft() {
    return left;
  }

  public Expression getRight() {
    return right;
  }

  public void setLeft(final Identifier left) {
    this.left = left;
  }

  public void setRight(final Expression right) {
    this.right = right;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((left == null) ? 0 : left.hashCode());
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
    Assignment other = (Assignment) obj;
    if (left == null) {
      if (other.left != null) {
        return false;
      }
    } else if (!left.equals(other.left)) {
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

  @Override
  public String toString() {
    return left.toString() + ":=" + right.toString();
  }
}
