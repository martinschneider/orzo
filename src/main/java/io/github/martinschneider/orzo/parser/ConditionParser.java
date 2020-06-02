package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Comparator;
import io.github.martinschneider.orzo.parser.productions.Condition;
import io.github.martinschneider.orzo.parser.productions.Expression;

public class ConditionParser implements ProdParser<Condition> {
  private ParserContext ctx;

  public ConditionParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Condition parse(TokenList tokens) {
    Expression left;
    Comparator operator;
    Expression right;
    int idx = tokens.idx();
    left = ctx.exprParser.parse(tokens);
    if (left != null && tokens.curr() instanceof Comparator) {
      operator = (Comparator) tokens.curr();
      tokens.next();
      right = ctx.exprParser.parse(tokens);
      if (right != null) {
        return new Condition(left, operator, right);
      }
    }
    tokens.setIdx(idx);
    return null;
  }
}
