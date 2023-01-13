package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.SYMBOL;

import io.github.martinschneider.orzo.lexer.TokenList;

public class ArrayDefParser {

  public byte parse(TokenList tokens) {
    byte array = 0;
    while (tokens.curr().eq(SYMBOL, LBRAK)) {
      tokens.next();
      if (tokens.curr().eq(SYMBOL, RBRAK)) {
        array++;
      } else {
        return 0;
      }
      tokens.next();
    }
    return array;
  }
}
