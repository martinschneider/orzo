package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keyword.FOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.List;

public class ForParser implements ProdParser<ForStatement> {
  ParserContext ctx;
  private static final String LOG_NAME = "parse for";

  public ForParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ForStatement parse(TokenList tokens) {
    Statement initialization;
    Expression condition;
    Statement loopStatement;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    if (tokens.curr().eq(keyword(FOR))) {
      tokens.next();
      if (!tokens.curr().eq(sym(LPAREN))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(
            LOG_NAME, sym(LPAREN), tokens, new RuntimeException().getStackTrace());
      }
      tokens.next();
      initialization = ctx.stmtParser.parse(tokens);
      if (initialization == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(
            LOG_NAME, "missing initialisation", new RuntimeException().getStackTrace());
        return null;
      }
      condition = ctx.exprParser.parse(tokens);
      if (condition == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(LOG_NAME, "missing condition", new RuntimeException().getStackTrace());
      }
      tokens.next();
      // TODO: support stmtSequence
      loopStatement = ctx.stmtParser.parse(tokens);
      if (loopStatement == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(
            LOG_NAME, "missing loop statement", new RuntimeException().getStackTrace());
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(
            LOG_NAME, sym(RPAREN), tokens, new RuntimeException().getStackTrace());
      }
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(
            LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
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
      } else {
        tokens.next();
      }
      return new ForStatement(initialization, condition, loopStatement, body);
    } else {
      return null;
    }
  }
}
