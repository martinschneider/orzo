package io.github.martinschneider.kommpeiler.parser.productions;

import java.util.List;

/** DoStatement */
public class DoStatement extends ConditionalStatement {
  private Factor condition;
  private List<Statement> body;

  /**
   * @param condition condition
   * @param body body
   */
  public DoStatement(final Factor condition, final List<Statement> body) {
    this.condition = condition;
    this.body = body;
  }

  /** {@inheritDoc} * */
  @Override
  public void setCondition(final Factor condition) {
    this.condition = condition;
  }

  /** {@inheritDoc} * */
  @Override
  public Factor getCondition() {
    return condition;
  }

  /** {@inheritDoc} * */
  public void setBody(final List<Statement> body) {
    this.body = body;
  }

  /** {@inheritDoc} * */
  @Override
  public List<Statement> getBody() {
    return body;
  }
}
