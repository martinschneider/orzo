package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Type;

public class CastParser implements ProdParser<Type> {

  private ParserContext ctx;

  public CastParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Type parse(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(sym(LPAREN))) {
      tokens.next();
      if (!(tokens.curr().isType())) {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
      Type type = Type.of(tokens.curr().val);
      tokens.next();
      if (tokens.curr().eq(sym(RPAREN))) {
        tokens.next();
        return type;
      } else {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
    }
    return null;
  }
}
