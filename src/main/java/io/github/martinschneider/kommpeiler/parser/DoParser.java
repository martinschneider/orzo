package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.DO;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.WHILE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.Keyword;
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
    Condition condition;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    if (tokens.curr() instanceof Keyword && tokens.curr().eq(keyword(DO))) {
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        tokens.prev();
        ctx.errors.missingExpected(LOG_NAME, sym(LBRACE), tokens);
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        body = new ArrayList<>();
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        tokens.prev();
        ctx.errors.missingExpected(LOG_NAME, sym(RBRACE), tokens);
      }
      tokens.next();
      if (!tokens.curr().eq(keyword(WHILE))) {
        tokens.prev();
        ctx.errors.missingExpected(LOG_NAME, keyword(WHILE), tokens);
      }
      tokens.next();
      if (!tokens.curr().eq(sym(LPAREN))) {
        tokens.prev();
        ctx.errors.missingExpected(LOG_NAME, sym(LPAREN), tokens);
      }
      tokens.next();
      condition = ctx.condParser.parse(tokens);
      if (condition == null) {
        tokens.prev();
        ctx.errors.addError(LOG_NAME, "missing condition");
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        tokens.prev();
        ctx.errors.missingExpected(LOG_NAME, sym(RPAREN), tokens);
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
