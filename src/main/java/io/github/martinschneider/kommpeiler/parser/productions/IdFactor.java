package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;

/**
 * IdFactor
 *
 * @author Martin Schneider
 */
public class IdFactor implements Factor {
  private Selector selector;
  private Token token;

  /** empty constructor */
  public IdFactor() {}

  /** @param token token */
  public IdFactor(final Token token) {
    this.setToken(token);
    if (token instanceof Identifier) {
      this.setSelector(((Identifier) token).getSelector());
    }
  }

  public Selector getSelector() {
    return selector;
  }

  public Token getToken() {
    return token;
  }

  @Override
  public Object getValue() {
    return token.getValue();
  }

  public void setSelector(final Selector selector) {
    this.selector = selector;
  }

  public void setToken(final Token token) {
    this.token = token;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return token.toString();
  }
}
