package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class SwitchCase {
  public List<Expression> labels; // null/empty = default case
  public List<Statement> body;

  public SwitchCase(List<Expression> labels, List<Statement> body) {
    this.labels = labels;
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
