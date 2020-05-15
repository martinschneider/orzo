package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;

public abstract class ConditionalStatement implements Statement {
  private List<Statement> body;
  private Condition condition;

  public ConditionalStatement() {}

  public ConditionalStatement(final Identifier left, final Expression right) {
    super();
  }

  public List<Statement> getBody() {
    return body;
  }

  public Condition getCondition() {
    return condition;
  }

  public void setBody(final List<Statement> body) {
    this.body = body;
  }

  public void setCondition(final Condition condition) {
    this.condition = condition;
  }
}
