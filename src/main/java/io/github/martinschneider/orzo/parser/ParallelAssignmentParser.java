package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ParallelAssignment;
import java.util.ArrayList;
import java.util.List;

public class ParallelAssignmentParser implements ProdParser<ParallelAssignment> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse parallel assignment";

  public ParallelAssignmentParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ParallelAssignment parse(TokenList tokens) {
    int idx = tokens.idx();
    List<Identifier> left = new ArrayList<>();
    List<Expression> right = new ArrayList<>();
    while (tokens.curr() instanceof Identifier || tokens.curr().eq(sym(COMMA))) {
      if (tokens.curr() instanceof Identifier) {
        left.add((Identifier) tokens.curr());
      }
      tokens.next();
    }
    if (!tokens.curr().eq(op(ASSIGN))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    Expression expression;
    while ((expression = ctx.exprParser.parse(tokens)) != null || tokens.curr().eq(sym(COMMA))) {
      if (expression != null) {
        right.add(expression);
      } else {
        tokens.next();
      }
    }
    if (left.size() != right.size()) {
      ctx.errors.addError(
          LOG_NAME,
          "left and right side must have the same number of variables in parallel assignment");
      tokens.setIdx(idx);
      return null;
    }
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    return new ParallelAssignment(left, right);
  }
}
