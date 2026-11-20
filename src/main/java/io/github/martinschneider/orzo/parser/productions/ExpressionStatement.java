package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;

/**
 * An expression evaluated for its side effects only, e.g. "arr[i].append(x);" - used for call
 * chains the dedicated statement parsers don't handle.
 */
public class ExpressionStatement implements Statement {
  public Expression expr;

  public ExpressionStatement(Expression expr) {
    this.expr = expr;
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
