package io.github.martinschneider.kommpeiler.parser.productions;

import java.util.List;

/** IfStatement */
public class IfStatement extends ConditionalStatement {
  private Factor condition;
  private List<Statement> body;

  /**
   * @param condition condition
   * @param body body
   */
  public IfStatement(final Factor condition, final List<Statement> body) {
    super();
    this.condition = condition;
    this.body = body;
  }

  @Override
  public void setCondition(final Factor condition) {
    this.condition = condition;
  }

  @Override
  public Factor getCondition() {
    return condition;
  }

  @Override
  public void setBody(final List<Statement> body) {
    this.body = body;
  }

  @Override
  public List<Statement> getBody() {
    return body;
  }
}
