package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.THROW;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.ThrowStatement;

public class ThrowParser implements ProdParser<ThrowStatement> {
  private ParserContext ctx;

  public ThrowParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ThrowStatement parse(TokenList tokens) {
    if (!tokens.curr().eq(keyword(THROW))) {
      return null;
    }
    int idx = tokens.idx();
    tokens.next();
    ConstructorCall cc = ctx.constrCallParser.parse(tokens);
    if (cc == null) {
      tokens.setIdx(idx);
      return null;
    }
    return new ThrowStatement(cc);
  }
}
