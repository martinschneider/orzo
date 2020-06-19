package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Condition;
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
    Condition condition;
    Statement loopStatement;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    if (tokens.curr().eq(keyword(FOR))) {
      tokens.next();
      if (!tokens.curr().eq(sym(LPAREN))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(LOG_NAME, sym(LPAREN), tokens);
      }
      tokens.next();
      initialization = ctx.stmtParser.parse(tokens);
      if (initialization == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(LOG_NAME, "missing initialisation");
        return null;
      }
      condition = ctx.condParser.parse(tokens);
      if (condition == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(LOG_NAME, "missing condition");
      }
      tokens.next();
      // TODO: support stmtSequence
      loopStatement = ctx.stmtParser.parse(tokens);
      if (loopStatement == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(LOG_NAME, "missing loop statement");
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(LOG_NAME, sym(RPAREN), tokens);
      }
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(LOG_NAME, sym(LBRACE), tokens);
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(LOG_NAME, "missing body");
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(LOG_NAME, sym(RBRACE), tokens);
      } else {
        tokens.next();
      }
      return new ForStatement(initialization, condition, loopStatement, body);
    } else {
      return null;
    }
  }
}
