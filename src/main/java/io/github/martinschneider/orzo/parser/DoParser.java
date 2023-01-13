package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keyword.DO;
import static io.github.martinschneider.orzo.lexer.tokens.Keyword.WHILE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.*;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoParser implements ProdParser<DoStatement> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse do";

  public DoParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public DoStatement parse(TokenList tokens) {
    Expression condition;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    if (tokens.curr().eq(KEYWORD,DO)) {
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        tokens.prev();
        ctx.errors.missingExpected(
            LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        body = new ArrayList<>();
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        tokens.prev();
        ctx.errors.missingExpected(
            LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
      }
      tokens.next();
      if (!tokens.curr().eq(keyword(WHILE))) {
        tokens.prev();
        ctx.errors.missingExpected(
            LOG_NAME, keyword(WHILE), tokens, new RuntimeException().getStackTrace());
      }
      tokens.next();
      if (!tokens.curr().eq(sym(LPAREN))) {
        tokens.prev();
        ctx.errors.missingExpected(
            LOG_NAME, sym(LPAREN), tokens, new RuntimeException().getStackTrace());
      }
      tokens.next();
      condition = ctx.exprParser.parse(tokens);
      if (condition == null) {
        tokens.prev();
        ctx.errors.addError(LOG_NAME, "missing condition", new RuntimeException().getStackTrace());
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        tokens.prev();
        ctx.errors.missingExpected(
            LOG_NAME, sym(RPAREN), tokens, new RuntimeException().getStackTrace());
      } else {
        tokens.next();
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
      }
      return new DoStatement(condition, body);
    } else {
      return null;
    }
  }
}
