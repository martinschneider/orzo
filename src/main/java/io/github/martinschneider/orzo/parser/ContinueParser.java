package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CONTINUE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Continue;

public class ContinueParser implements ProdParser<Continue> {
  @Override
  public Continue parse(TokenList tokens) {
    if (tokens.curr().eq(keyword(CONTINUE))) {
      if (tokens.next().eq(sym(SEMICOLON))) {
        tokens.next();
        return new Continue();
      } else {
        tokens.prev();
      }
    }
    return null;
  }
}
