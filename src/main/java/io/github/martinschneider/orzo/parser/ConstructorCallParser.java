package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Type;
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
    return parse(tokens, false);
  }

  public ConstructorCall parse(TokenList tokens, boolean saveSemicolon) {
    int idx = tokens.idx();
    if (!(tokens.curr() instanceof Keyword && ((Keyword) tokens.curr()).eq(keyword("new")))) {
      return null;
    }
    tokens.next();
    String typeName = null;
    if (tokens.curr() instanceof Identifier) {
      typeName = ((Identifier) tokens.curr()).id();
      tokens.next();
    } else if (tokens.curr() instanceof Type) {
      typeName = ((Type) tokens.curr()).name;
      tokens.next();
    } else {
      ctx.errors.addError(
          LOG_NAME,
          "identifier or type expected after \"new\"",
          new RuntimeException().getStackTrace());
      tokens.prev();
      return null;
    }
    List<Expression> args = ctx.methodCallParser.parseArgs(tokens);
    if (args != null) {
      if (tokens.curr().eq(sym(SEMICOLON)) && !saveSemicolon) {
        tokens.next();
      }
      return new ConstructorCall(typeName, args);
    } else {
      ctx.errors.tokenIdx = tokens.idx();
      tokens.setIdx(idx);
      return null;
    }
  }
}
