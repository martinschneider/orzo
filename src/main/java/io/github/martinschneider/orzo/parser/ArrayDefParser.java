package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;

public class ArrayDefParser {

  public byte parse(TokenList tokens) {
    byte array = 0;
    while (tokens.curr().eq(sym(LBRAK))) {
      tokens.next();
      if (tokens.curr().eq(sym(RBRAK))) {
        array++;
      } else {
        return 0;
      }
      tokens.next();
    }
    return array;
  }
}
