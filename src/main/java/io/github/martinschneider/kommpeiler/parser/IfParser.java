package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.ELSE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.IF;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.IfBlock;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.ArrayList;
import java.util.List;

public class IfParser implements ProdParser<IfStatement> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse if";
  private static final String IF_BLOCK_LOG_NAME = "parse if block";
  private static final String ELSE_BLOCK_LOG_NAME = "parse else block";

  public IfParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public IfStatement parse(TokenList tokens) {
    IfStatement ifStatement;
    List<IfBlock> ifBlocks = new ArrayList<>();
    IfBlock ifBlock = parseIfBlock(tokens, keyword(IF));
    if (ifBlock != null) {
      ifBlocks.add(ifBlock);
      IfBlock elseIfBlock;
      int idx1 = tokens.idx();
      int idx2 = idx1;
      while ((elseIfBlock = parseIfBlock(tokens, keyword(ELSE), keyword(IF))) != null) {
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

  IfBlock parseIfBlock(TokenList tokens, Token... expectedTokens) {
    Condition condition;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    for (Token expectedToken : expectedTokens) {
      if (!tokens.curr().eq(expectedToken)) {
        return null;
      }
      tokens.next();
    }
    if (!tokens.curr().eq(sym(LPAREN))) {
      tokens.prev();
      ctx.errors.missingExpected(IF_BLOCK_LOG_NAME, sym(LPAREN), tokens);
    }
    tokens.next();
    condition = ctx.condParser.parse(tokens);
    if (condition == null) {
      tokens.prev();
      ctx.errors.addError(IF_BLOCK_LOG_NAME, "missing condition");
    }
    if (!tokens.curr().eq(sym(RPAREN))) {
      tokens.prev();
      ctx.errors.missingExpected(LOG_NAME, sym(RPAREN), tokens);
    }
    tokens.next();
    if (!tokens.curr().eq(sym(LBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(IF_BLOCK_LOG_NAME, sym(LBRACE), tokens);
    }
    tokens.next();
    body = ctx.stmtParser.parseStmtSeq(tokens);
    if (body == null) {
      ctx.errors.addError(IF_BLOCK_LOG_NAME, "missing body");
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(IF_BLOCK_LOG_NAME, sym(RBRACE), tokens);
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
    int startIdx = tokens.idx();
    tokens.next();
    if (!tokens.curr().eq(sym(LBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(ELSE_BLOCK_LOG_NAME, sym(LBRACE), tokens);
    }
    tokens.next();
    body = ctx.stmtParser.parseStmtSeq(tokens);
    if (body == null) {
      ctx.errors.addError(LOG_NAME, "missing body");
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      tokens.prev();
      ctx.errors.missingExpected(ELSE_BLOCK_LOG_NAME, sym(RBRACE), tokens);
    } else {
      tokens.next();
    }
    if (body != null) {
      return new IfBlock(null, body);
    }
    return null;
  }
}
