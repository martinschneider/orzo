package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.lexer.TokenList;

public interface ProdParser<T> {
  /**
   * Invariant: If there is a match, the token index must point to the next token after that match.
   * If there is no match, the token index must be restored to the val before parse has been called.
   *
   * @return a production if there is a match, null otherwise
   */
  T parse(TokenList tokens);
}
