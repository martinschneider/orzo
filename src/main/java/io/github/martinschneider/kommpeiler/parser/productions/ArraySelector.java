package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * ArraySelector
 *
 * @author Martin Schneider
 */
public class ArraySelector extends Selector {
  private Expression expression;

  public void setExpression(final Expression expression) {
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }

  /** @param expression expression */
  public ArraySelector(final Expression expression) {
    super();
    this.expression = expression;
  }
}
