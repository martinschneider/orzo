package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class ForEachStatement implements Statement {
  public String elemType;
  public Identifier elemVar;
  public Expression iterable;
  public List<Statement> body;

  public ForEachStatement(
      String elemType, Identifier elemVar, Expression iterable, List<Statement> body) {
    this.elemType = elemType;
    this.elemVar = elemVar;
    this.iterable = iterable;
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
