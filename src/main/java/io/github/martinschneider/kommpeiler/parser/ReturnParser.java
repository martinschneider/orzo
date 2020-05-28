package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.RETURN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.scanner.TokenList;

public class ReturnParser implements ProdParser<ReturnStatement> {
  private ParserContext ctx;

  public ReturnParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ReturnStatement parse(TokenList tokens) {
    if (tokens.curr().eq(keyword(RETURN))) {
      tokens.next();
      Expression expression = ctx.exprParser.parse(tokens);
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
        return new ReturnStatement(expression);
      } else {
        // TODO: log error
      }
    }
    return null;
  }
}
