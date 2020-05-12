package io.github.martinschneider.kommpeiler.scanner.tokens;

/**
 * Operator
 *
 * @author Martin Schneider
 */
public class Operator extends Sym {
  /** @param value value */
  public Operator(final Operators value) {
    super(value);
  }

  public Operators opValue() {
    return ((Operators) getValue());
  }

  public int precedence() {
    return ((Operators) getValue()).getPrecedence();
  }
}
