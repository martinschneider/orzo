package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.ArrayList;
import java.util.List;

public class ArraySelectorParser implements ProdParser<ArraySelector> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse array selector";

  public ArraySelectorParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ArraySelector parse(TokenList tokens) {
    return parse(tokens, false);
  }

  public ArraySelector parse(TokenList tokens, boolean removeTokens) {
    List<Expression> expressions = new ArrayList<>();
    Expression expr;
    int startIdx = tokens.idx();
    while (tokens.curr().eq(sym(LBRAK))) {
      tokens.next();
      if ((expr = ctx.exprParser.parse(tokens)) != null) {
        expressions.add(expr);
        if (!tokens.curr().eq(sym(RBRAK))) {
          ctx.errors.missingExpected(
              LOG_NAME, sym(RBRAK), tokens, new RuntimeException().getStackTrace());
        }
        tokens.next();
      } else {
        tokens.prev();
        return null;
      }
    }
    if (expressions.isEmpty()) {
      return null;
    }
    // this is used when called from the context of an expression
    if (removeTokens) {
      int size = tokens.list().subList(startIdx, tokens.idx()).size();
      tokens.list().subList(startIdx, tokens.idx()).clear();
      tokens.bw(size);
    }
    return new ArraySelector(expressions);
  }
}
