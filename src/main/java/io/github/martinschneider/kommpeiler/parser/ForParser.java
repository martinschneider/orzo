package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.FOR;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.ForStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import java.util.List;

public class ForParser implements ProdParser<ForStatement> {
  private ParserContext ctx;

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
        tokens.prev();
        ctx.errors.addParserError("for must be followed by (");
      }
      tokens.next();
      initialization = ctx.stmtParser.parse(tokens);
      if (initialization == null) {
        tokens.bw(2);
        ctx.errors.addParserError("for stmt must contain an initialization stmt");
        return null;
      }
      condition = ctx.condParser.parse(tokens);
      if (condition == null) {
        tokens.prev();
        ctx.errors.addParserError("for stmt must contain a condition");
      }
      tokens.next();
      // TODO: support stmtSequence
      loopStatement = ctx.stmtParser.parse(tokens);
      if (loopStatement == null) {
        tokens.prev();
        ctx.errors.addParserError("for stmt must contain a loop stmt");
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        tokens.prev();
        ctx.errors.addParserError("missing ) in for stmt");
      }
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        tokens.prev();
        ctx.errors.addParserError("missing { in for stmt");
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        ctx.errors.addParserError("invalid body of for stmt");
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        tokens.prev();
        ctx.errors.addParserError("missing } in for-clause");
      } else {
        tokens.next();
      }
      return new ForStatement(initialization, condition, loopStatement, body);
    } else {
      return null;
    }
  }
}
