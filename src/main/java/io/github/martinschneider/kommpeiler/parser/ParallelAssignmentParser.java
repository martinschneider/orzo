package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
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
