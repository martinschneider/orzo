package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Increment;
import java.util.List;

public class PostIncrementParser implements ProdParser<Increment> {
  private ParserContext ctx;

  public PostIncrementParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Increment parse(TokenList tokens) {
    Identifier id;
    Operator op;
    int idx = tokens.idx();
    if (tokens.curr() instanceof Identifier) {
      id = (Identifier) tokens.curr();
      tokens.next();
      id.arrSel = ctx.arraySelectorParser.parse(tokens);
    } else {
      return null;
    }
    if (tokens.curr() instanceof Operator) {
      if (tokens.curr().eq(op(POST_INCREMENT))) {
        op = op(POST_INCREMENT);
        tokens.next();
      } else if (tokens.curr().eq(op(POST_DECREMENT))) {
        op = op(POST_DECREMENT);
        tokens.next();
      } else {
        tokens.prev();
        return null;
      }
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
      return new Increment(new Expression(List.of(id, op)));
    } else {
      tokens.setIdx(idx);
      return null;
    }
  }
}
