package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keyword.RETURN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;

public class RetParser implements ProdParser<ReturnStatement> {
  private ParserContext ctx;

  public RetParser(ParserContext ctx) {
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
