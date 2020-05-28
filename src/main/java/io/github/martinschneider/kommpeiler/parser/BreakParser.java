package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.BREAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.Break;
import io.github.martinschneider.kommpeiler.scanner.TokenList;

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
