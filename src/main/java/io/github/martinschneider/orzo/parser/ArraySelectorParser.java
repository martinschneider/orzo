package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
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

  /** "[i]" or "[i][j]", e.g. of a declaration "int a[3];" */
  @Override
  public ArraySelector parse(TokenList tokens) {
    List<Expression> expressions = new ArrayList<>();
    while (tokens.curr().eq(sym(LBRAK))) {
      tokens.next();
      Expression expr = ctx.exprParser.parse(tokens);
      if (expr == null) {
        tokens.prev();
        return null;
      }
      expressions.add(expr);
      if (!tokens.curr().eq(sym(RBRAK))) {
        ctx.errors.missingExpected(
            LOG_NAME, sym(RBRAK), tokens, new RuntimeException().getStackTrace());
      }
      tokens.next();
    }
    if (expressions.isEmpty()) {
      return null;
    }
    return new ArraySelector(expressions);
  }
}
