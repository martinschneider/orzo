package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;

/**
 * ConditionalStatement
 *
 * @author Martin Schneider
 */
public abstract class ConditionalStatement implements Statement {
  private List<Statement> body;
  private Condition condition;

  /** empty constructor */
  public ConditionalStatement() {}

  /**
   * @param left left
   * @param right right
   */
  public ConditionalStatement(final Identifier left, final Expression right) {
    super();
  }

  /** @return body */
  List<Statement> getBody() {
    return body;
  }

  /** @return condition */
  Condition getCondition() {
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

  /**
   * set the condition
   *
   * @param condition condition
   */
  void setCondition(final Condition condition) {
    this.condition = condition;
  }
}
