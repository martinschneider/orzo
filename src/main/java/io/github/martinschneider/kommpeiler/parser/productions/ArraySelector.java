package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * ArraySelector
 *
 * @author Martin Schneider
 */
public class ArraySelector extends Selector {
  private Factor expression;

  public void setExpression(final Factor expression) {
    this.expression = expression;
  }

  public Factor getExpression() {
    return expression;
  }

  /** @param expression expression */
  public ArraySelector(final Factor expression) {
    super();
    this.expression = expression;
  }
}
