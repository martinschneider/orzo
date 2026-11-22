package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.BREAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Break;

public class BreakParser implements ProdParser<Break> {
  @Override
  public Break parse(TokenList tokens) {
    if (!tokens.curr().eq(keyword(BREAK))) {
      return null;
    }
    tokens.next();
    Break stmt = new Break();
    if (tokens.curr() instanceof Identifier) {
      // labeled, e.g. "break outer;"
      stmt.label = tokens.curr().val.toString();
      tokens.next();
    }
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next(); // (a missing ';' is reported by StatementParser)
    }
    return stmt;
  }
}
