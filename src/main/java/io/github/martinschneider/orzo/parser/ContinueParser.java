package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CONTINUE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Continue;

public class ContinueParser implements ProdParser<Continue> {
  @Override
  public Continue parse(TokenList tokens) {
    if (!tokens.curr().eq(keyword(CONTINUE))) {
      return null;
    }
    tokens.next();
    Continue stmt = new Continue();
    if (tokens.curr() instanceof Identifier) {
      // labeled, e.g. "continue outer;"
      stmt.label = tokens.curr().val.toString();
      tokens.next();
    }
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next(); // (a missing ';' is reported by StatementParser)
    }
    return stmt;
  }
}
