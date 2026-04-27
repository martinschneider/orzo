package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class Assignment implements Statement {
  public List<Identifier> left;
  public List<Expression> right;

  public Assignment(List<Identifier> left, List<Expression> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return ObjectUtils.equals(this, obj);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
