package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.WHILE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.WhileStatement;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import java.util.ArrayList;
import java.util.List;

public class WhileParser implements ProdParser<WhileStatement> {
  private ParserContext ctx;

  public WhileParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public WhileStatement parse(TokenList tokens) {
    Condition condition;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    if (tokens.curr().eq(keyword(WHILE))) {
      tokens.next();
      if (!tokens.curr().eq(sym(LPAREN))) {
        tokens.prev();
        ctx.errors.addParserError("while must be followed by (");
      }
      tokens.next();
      condition = ctx.condParser.parse(tokens);
      if (condition == null) {
        tokens.bw(2);
        ctx.errors.addParserError("while( must be followed by a valid expression");
        return null;
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        tokens.prev();
        ctx.errors.addParserError("missing ) in while-clause");
      }
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        tokens.prev();
        ctx.errors.addParserError("missing { in while-clause");
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        body = new ArrayList<>();
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        tokens.prev();
        ctx.errors.addParserError("missing } in while-clause");
      }
      tokens.next();
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
      return new WhileStatement(condition, body);
    } else {
      return null;
    }
  }
}
