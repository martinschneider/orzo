package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class StatementParser implements ProdParser<Statement> {
  private ParserContext ctx;

  public StatementParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  public List<Statement> parseStmtSeq(TokenList tokens) {
    List<Statement> stmtSequence = new ArrayList<>();
    Statement stmt;
    while ((stmt = parse(tokens)) != null) {
      stmtSequence.add(stmt);
    }
    return (stmtSequence.size() == 0) ? null : stmtSequence;
  }

  @Override
  public Statement parse(TokenList tokens) {
    Statement stmt;
    if ((stmt = ctx.assignParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.postIncrementParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.preIncrementParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.ifParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.doParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.whileParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.forParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.repeatParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.declParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.methodCallParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.retParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.breakParser.parse(tokens)) != null) {
      return stmt;
    } else {
      return null;
    }
  }
}
