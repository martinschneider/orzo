package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;

/**
 * ConditionalStatement
 *
 * @author Martin Schneider
 */
public abstract class ConditionalStatement extends Statement {

  private Factor condition;
  private List<Statement> body;

  /**
   * @param left left
   * @param right right
   */
  public ConditionalStatement(final Identifier left, final Factor right) {
    super();
  }

  /** empty constructor */
  public ConditionalStatement() {}

  /**
   * set the condition
   *
   * @param condition condition
   */
  void setCondition(final Factor condition) {
    this.condition = condition;
  }

  /** @return condition */
  Factor getCondition() {
    return condition;
  }

  /**
   * set the body
   *
   * @param body body
   */
  void setBody(final List<Statement> body) {
    this.body = body;
  }

  /** @return body */
  List<Statement> getBody() {
    return body;
  }
}
