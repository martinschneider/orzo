package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.EmptyStatement;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
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
        ctx.errors.missingExpected(
            LOG_NAME, sym(LPAREN), tokens, new RuntimeException().getStackTrace());
        tokens.next(sym(RBRACE));
      }
      tokens.next();
      if (tokens.curr().eq(sym(SEMICOLON))) {
        // no initialization, e.g. "for (; i < n; i++)"
        tokens.next();
        initialization = new EmptyStatement();
      } else {
        initialization = ctx.stmtParser.parse(tokens);
      }
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
      loopStatement = ctx.stmtParser.parseWithoutSemicolon(tokens);
      if (loopStatement == null) {
        tokens.next(sym(RBRACE));
        ctx.errors.addError(
            LOG_NAME, "missing loop statement", new RuntimeException().getStackTrace());
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        ctx.errors.missingExpected(
            LOG_NAME, sym(RPAREN), tokens, new RuntimeException().getStackTrace());
        tokens.next(sym(RBRACE));
      }
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        // single-statement body without braces, e.g. "for (...) total += i;"
        Statement stmt = ctx.stmtParser.parse(tokens);
        if (stmt == null) {
          ctx.errors.missingExpected(
              LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
          tokens.next(sym(RBRACE));
          return null;
        }
        body = new ArrayList<>();
        body.add(stmt);
        return new ForStatement(initialization, condition, loopStatement, body);
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        body = new ArrayList<>();
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        ctx.errors.missingExpected(
            LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
        tokens.next(sym(RBRACE));
      } else {
        tokens.next();
      }
      return new ForStatement(initialization, condition, loopStatement, body);
    } else {
      return null;
    }
  }
}
