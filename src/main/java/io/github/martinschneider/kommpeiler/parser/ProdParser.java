package io.github.martinschneider.kommpeiler.parser;

import io.github.martinschneider.kommpeiler.lexer.TokenList;

public interface ProdParser<T> {
  /**
   * Invariant: If there is a match, the token index must point to the next token after that match.
   * If there is no match, the token index must be restored to the value before parse has been
   * called.
   *
   * @return a production if there is a match, null otherwise
   */
  T parse(TokenList tokens);
}
