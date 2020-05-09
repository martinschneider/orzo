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

  private ValueType valueType = ValueType.MEMORY;

  /** empty constructor */
  public IdFactor() {}

  @Override
  public Object getValue() {
    return token.getValue();
  }

  /** @param token token */
  public IdFactor(final Token token) {
    this.setToken(token);
    if (token instanceof Identifier) {
      this.setSelector(((Identifier) token).getSelector());
    }
  }

  public void setToken(final Token token) {
    this.token = token;
  }

  public Token getToken() {
    return token;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return token.toString();
  }

  public void setSelector(final Selector selector) {
    this.selector = selector;
  }

  public Selector getSelector() {
    return selector;
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  @Override
  public void setValueType(final ValueType valueType) {
    this.valueType = valueType;
  }
}
