package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CATCH;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINALLY;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.TRY;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.CatchBlock;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import java.util.ArrayList;
import java.util.List;

public class TryParser implements ProdParser<TryStatement> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse try stmt";

  public TryParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public TryStatement parse(TokenList tokens) {
    if (!tokens.curr().eq(keyword(TRY))) {
      return null;
    }
    tokens.next(); // consume 'try'
    if (!tokens.curr().eq(sym(LBRACE))) {
      ctx.errors.addError(
          LOG_NAME, "expected '{' after try", new RuntimeException().getStackTrace());
      return null;
    }
    tokens.next(); // consume '{'
    List<Statement> tryBody = ctx.stmtParser.parseStmtSeq(tokens);
    if (tryBody == null) {
      tryBody = new ArrayList<>();
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      ctx.errors.addError(
          LOG_NAME, "expected '}' after try body", new RuntimeException().getStackTrace());
      return null;
    }
    tokens.next(); // consume '}'

    List<CatchBlock> catchBlocks = new ArrayList<>();
    while (tokens.curr().eq(keyword(CATCH))) {
      tokens.next(); // consume 'catch'
      if (!tokens.curr().eq(sym(LPAREN))) {
        ctx.errors.addError(
            LOG_NAME, "expected '(' after catch", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '('
      String exceptionType = tokens.curr().val.toString();
      tokens.next(); // consume exception type
      String varName = tokens.curr().val.toString();
      tokens.next(); // consume variable name
      if (!tokens.curr().eq(sym(RPAREN))) {
        ctx.errors.addError(
            LOG_NAME, "expected ')' after catch parameter", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume ')'
      if (!tokens.curr().eq(sym(LBRACE))) {
        ctx.errors.addError(
            LOG_NAME, "expected '{' after catch header", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '{'
      List<Statement> catchBody = ctx.stmtParser.parseStmtSeq(tokens);
      if (catchBody == null) {
        catchBody = new ArrayList<>();
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        ctx.errors.addError(
            LOG_NAME, "expected '}' after catch body", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '}'
      catchBlocks.add(new CatchBlock(exceptionType, varName, catchBody));
    }

    // Skip finally block (not yet supported - just parse and discard)
    if (tokens.curr().eq(keyword(FINALLY))) {
      tokens.next(); // consume 'finally'
      if (tokens.curr().eq(sym(LBRACE))) {
        tokens.next(); // consume '{'
        ctx.stmtParser.parseStmtSeq(tokens); // parse and discard
        if (tokens.curr().eq(sym(RBRACE))) {
          tokens.next(); // consume '}'
        }
      }
    }

    if (catchBlocks.isEmpty()) {
      ctx.errors.addError(LOG_NAME, "try without catch", new RuntimeException().getStackTrace());
      return null;
    }
    return new TryStatement(tryBody, catchBlocks);
  }
}
