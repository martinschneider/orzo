package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.BREAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Break;

public class BreakParser implements ProdParser<Break> {
  @Override
  public Break parse(TokenList tokens) {
    if (tokens.curr().eq(keyword(BREAK))) {
      if (tokens.next().eq(sym(SEMICOLON))) {
        tokens.next();
        return new Break();
      } else {
        tokens.prev();
      }
    }
    return null;
  }
}
