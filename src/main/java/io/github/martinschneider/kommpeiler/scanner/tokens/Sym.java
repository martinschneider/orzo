package io.github.martinschneider.kommpeiler.scanner.tokens;

/**
 * Symbol
 *
 * @author Martin Schneider
 */
public class Sym extends Token {

  /** @param value value */
  public Sym(final TokenType value) {
    super(value.toString().toUpperCase());
  }
}
