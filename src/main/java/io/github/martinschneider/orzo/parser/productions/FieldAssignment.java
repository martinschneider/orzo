package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.util.ObjectUtils;

/**
 * An assignment to a field of an object computed by an expression, e.g. "map.get(k).count = 1;"
 * (target is the selector chain, ending in the field).
 */
public class FieldAssignment implements Statement {
  public Identifier target;
  public Expression value;

  public FieldAssignment(Identifier target, Expression value) {
    this.target = target;
    this.value = value;
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
