package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class ForEachParser implements ProdParser<ForEachStatement> {
  ParserContext ctx;
  private static final String LOG_NAME = "parse for-each";

  public ForEachParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ForEachStatement parse(TokenList tokens) {
    if (tokens.curr() == null || !tokens.curr().eq(keyword(FOR))) {
      return null;
    }
    int savedIdx = tokens.idx();
    tokens.next();
    if (!tokens.curr().eq(sym(LPAREN))) {
      tokens.setIdx(savedIdx);
      return null;
    }
    tokens.next();
    Statement initialization = ctx.stmtParser.parse(tokens);
    if (!(initialization instanceof ParallelDeclaration)) {
      tokens.setIdx(savedIdx);
      return null;
    }
    // Only enhanced for if colon follows the declaration
    if (!tokens.curr().eq(sym(COLON))) {
      tokens.setIdx(savedIdx);
      return null;
    }
    tokens.next();
    Expression iterable = ctx.exprParser.parse(tokens);
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
    List<Statement> body = ctx.stmtParser.parseStmtSeq(tokens);
    if (body == null) {
      body = new ArrayList<>();
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      tokens.next(sym(RBRACE));
      ctx.errors.missingExpected(
          LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
    } else {
      tokens.next();
    }
    ParallelDeclaration decl = (ParallelDeclaration) initialization;
    Declaration first = decl.declarations.get(0);
    return new ForEachStatement(first.type, first.name, iterable, body);
  }
}
