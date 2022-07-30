package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.List;

public class ConstructorCallParser implements ProdParser<ConstructorCall> {

  private ParserContext ctx;

  private static final String LOG_NAME = "parse constructor call";

  public ConstructorCallParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ConstructorCall parse(TokenList tokens) {
    int idx = tokens.idx();
    if (!(tokens.curr() instanceof Keyword && ((Keyword) tokens.curr()).eq(keyword("new")))) {
      return null;
    }
    tokens.next();
    Identifier type = null;
    if (!(tokens.curr() instanceof Identifier)) {
      ctx.errors.addError(LOG_NAME, "identifier expected after \"new\"");
      tokens.prev();
      return null;
    } else {
      type = (Identifier) tokens.curr();
      tokens.next();
      List<Expression> args = ctx.methodCallParser.parseArgs(tokens);
      if (args != null) {
        return new ConstructorCall(type.id(), args);
      } else {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
    }
  }
}
