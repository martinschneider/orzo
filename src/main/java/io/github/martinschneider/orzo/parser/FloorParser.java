package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LFLOOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RFLOOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.MethodCall;

public class FloorParser implements ProdParser<MethodCall> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse floor function";

  public FloorParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public MethodCall parse(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(sym(LFLOOR))) {
      tokens.replace(id("Math.round"));
      tokens.insert(sym(LPAREN));
      tokens.insert(id("Math.floor"));
      tokens.insert(sym(LPAREN));
      tokens.fw(3);
      ctx.exprParser.parse(tokens);
      if (tokens.curr().eq(sym(RFLOOR))) {
        tokens.replace(sym(RPAREN));
        tokens.insert(sym(RPAREN));
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      } else {
        ctx.errors.addError(LOG_NAME, "missing closing ⌋", new RuntimeException().getStackTrace());
      }
    }
    return null;
  }
}
