package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import java.util.List;

public class ParallelAssignment implements Statement {
  private List<Identifier> left;
  private List<Expression> right;

  public ParallelAssignment(List<Identifier> left, List<Expression> right) {
    super();
    this.left = left;
    this.right = right;
  }

  public List<Identifier> getLeft() {
    return left;
  }

  public void setLeft(List<Identifier> left) {
    this.left = left;
  }

  public List<Expression> getRight() {
    return right;
  }

  public void setRight(List<Expression> right) {
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
    ParallelAssignment other = (ParallelAssignment) obj;
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
    return left + "=" + right;
  }
}
