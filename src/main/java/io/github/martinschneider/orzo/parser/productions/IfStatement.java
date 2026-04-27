package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class IfStatement implements Statement {
  public List<IfBlock> ifBlks;
  public boolean hasElse;

  public IfStatement(List<IfBlock> ifBlks, boolean hasElse) {
    this.ifBlks = ifBlks;
    this.hasElse = hasElse;
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
