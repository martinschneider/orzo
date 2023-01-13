package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keyword.*;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.NEGATE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.*;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.*;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class IfParser implements ProdParser<IfStatement> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse if stmt";
  private static final String IF_BLOCK_LOG_NAME = "parse if block";
  private static final String ELSE_BLOCK_LOG_NAME = "parse else block";

  public IfParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public IfStatement parse(TokenList tokens) {
    IfStatement stmt = parse(tokens, IF, false);
    if (stmt != null) {
      return stmt;
    }
    return parse(tokens, UNLESS, true);
  }

  public IfStatement parse(TokenList tokens, Keyword keyword, boolean negate) {
    IfStatement ifStatement;
    List<IfBlock> ifBlocks = new ArrayList<>();
    IfBlock ifBlock = parseIfBlock(tokens, negate, keyword);
    if (ifBlock != null) {
      ifBlocks.add(ifBlock);
      IfBlock elseIfBlock;
      int idx1 = tokens.idx();
      int idx2 = idx1;
      while ((elseIfBlock = parseIfBlock(tokens, false, ELSE, IF)) != null) {
        ifBlocks.add(elseIfBlock);
        idx2 = tokens.idx();
      }
      tokens.setIdx(idx2);
      IfBlock elseBlock = parseElseBlock(tokens);
      if (elseBlock != null) {
        ifBlocks.add(elseBlock);
        ifStatement = new IfStatement(ifBlocks, true);
      } else {
        ifStatement = new IfStatement(ifBlocks, false);
      }
      return ifStatement;
    }
    return null;
  }

  IfBlock parseIfBlock(TokenList tokens, boolean negate, Keyword... expectedKeywords) {
    Expression condition;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    for (Keyword expectedKeyword : expectedKeywords) {
      if (!tokens.curr().eq(KEYWORD, expectedKeyword)) {
        return null;
      }
      tokens.next();
    }
    if (!tokens.curr().eq(sym(LPAREN))) {
      tokens.prev();
      ctx.errors.missingExpected(
          IF_BLOCK_LOG_NAME, sym(LPAREN), tokens, new RuntimeException().getStackTrace());
    }
    tokens.next();
    condition = ctx.exprParser.parse(tokens);
    if (negate) {
      condition.tokens.add(op(NEGATE));
    }
    if (condition == null) {
      tokens.prev();
      ctx.errors.addError(
          IF_BLOCK_LOG_NAME, "missing condition", new RuntimeException().getStackTrace());
    }
    if (!tokens.curr().eq(sym(RPAREN))) {
      tokens.prev();
      ctx.errors.missingExpected(
          LOG_NAME, sym(RPAREN), tokens, new RuntimeException().getStackTrace());
    }
    tokens.next();
    if (!tokens.curr().eq(sym(LBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(
          IF_BLOCK_LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
    }
    tokens.next();
    body = ctx.stmtParser.parseStmtSeq(tokens);
    if (body == null) {
      ctx.errors.addError(
          IF_BLOCK_LOG_NAME, "missing body", new RuntimeException().getStackTrace());
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(
          IF_BLOCK_LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
    } else {
      tokens.next();
    }
    if (condition != null & body != null) {
      return new IfBlock(condition, body);
    }
    return null;
  }

  IfBlock parseElseBlock(TokenList tokens) {
    List<Statement> body;
    if (!tokens.curr().eq(keyword(ELSE))) {
      return null;
    }
    tokens.next();
    if (!tokens.curr().eq(sym(LBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(
          ELSE_BLOCK_LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
    }
    tokens.next();
    body = ctx.stmtParser.parseStmtSeq(tokens);
    if (body == null) {
      ctx.errors.addError(LOG_NAME, "missing body", new RuntimeException().getStackTrace());
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(
          ELSE_BLOCK_LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
    } else {
      tokens.next();
    }
    if (body != null) {
      return new IfBlock(null, body);
    }
    return null;
  }
}
