package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

/** A "{ ... }" block statement, optionally labeled ("label: { ... break label; ... }"). */
public class Block implements Statement {
  public List<Statement> body;
  public String label;

  public Block(List<Statement> body) {
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
