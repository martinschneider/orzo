package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.REPEAT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.*;
import static io.github.martinschneider.orzo.lexer.tokens.Token.*;

import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.parser.productions.*;
import java.io.IOException;
import java.util.List;

public class RepeatParser implements ProdParser<LoopStatement> {
  private static final String LOG_NAME = "parse repeat";
  ParserContext ctx;

  public RepeatParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public LoopStatement parse(TokenList tokens) {
    Expression count;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    if (tokens.curr().eq(keyword(REPEAT))) {
      tokens.next();
      count = ctx.exprParser.parse(tokens);
      if (!tokens.curr().eq(sym(LBRACE))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(
            LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(LOG_NAME, "missing body", new RuntimeException().getStackTrace());
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(
            LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
        return null;
      } else {
        tokens.next();
      }
      if (count == null) {
        // infinite loop
        return new WhileStatement(new Expression(List.of(bool("true"))), body);
      }
      try {
        Lexer lexer = new Lexer();
        count.tokens.add(0, id("i"));
        count.tokens.add(op(Operators.LESS));
        return new ForStatement(
            ctx.stmtParser.parse(lexer.getTokens("int i=0")),
            count,
            ctx.stmtParser.parse(lexer.getTokens("i++")),
            body);
      } catch (IOException e) {
        // TODO: is this needed?
        ctx.errors.addError(
            LOG_NAME,
            String.format("unexpected error creating loop statement %s", e.getMessage()),
            new RuntimeException().getStackTrace());
        return null;
      }
    } else {
      return null;
    }
  }
}
