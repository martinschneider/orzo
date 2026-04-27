package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class IfBlock {
  public List<Statement> body;
  public Expression cond;

  public IfBlock(Expression cond, List<Statement> body) {
    this.cond = cond;
    this.body = body;
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
