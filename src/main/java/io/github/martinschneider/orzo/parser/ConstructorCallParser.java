package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.TokenType.KEYWORD;

import java.util.List;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Identifier;
import io.github.martinschneider.orzo.parser.productions.Type;

public class ConstructorCallParser implements ProdParser<ConstructorCall> {

  private ParserContext ctx;

  private static final String LOG_NAME = "parse constructor call";

  public ConstructorCallParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ConstructorCall parse(TokenList tokens) {
    int idx = tokens.idx();
    if (!(tokens.curr().eq(KEYWORD, Keyword.NEW))) {
      return null;
    }
    tokens.next();
    Type type = null;
    if (!(tokens.curr().isId())) {
      ctx.errors.addError(
          LOG_NAME, "identifier expected after \"new\"", new RuntimeException().getStackTrace());
      tokens.prev();
      return null;
    } else {
      type = Type.of(tokens.curr().val);
      tokens.next();
      List<Expression> args = ctx.methodCallParser.parseArgs(tokens);
      if (args != null) {
        return new ConstructorCall(type.name, args);
      } else {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
    }
  }
}
