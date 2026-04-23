package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.parser.productions.Expression;

public class TernaryExpression extends Token {
  public Expression condition;
  public Expression trueBranch;
  public Expression falseBranch;

  public TernaryExpression(Expression condition, Expression trueBranch, Expression falseBranch) {
    super("?:");
    this.condition = condition;
    this.trueBranch = trueBranch;
    this.falseBranch = falseBranch;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TernaryExpression other = (TernaryExpression) obj;
    return condition.equals(other.condition)
        && trueBranch.equals(other.trueBranch)
        && falseBranch.equals(other.falseBranch);
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + condition.hashCode();
    result = prime * result + trueBranch.hashCode();
    result = prime * result + falseBranch.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "(" + condition + " ? " + trueBranch + " : " + falseBranch + ")";
  }
}
