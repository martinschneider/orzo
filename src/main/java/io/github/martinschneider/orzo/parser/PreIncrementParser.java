package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import java.util.List;

public class PreIncrementParser implements ProdParser<IncrementStatement> {
  private ParserContext ctx;

  public PreIncrementParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public IncrementStatement parse(TokenList tokens) {
    Identifier id;
    Operator op;
    int idx = tokens.idx();
    if (tokens.curr() instanceof Operator) {
      if (tokens.curr().eq(op(PRE_INCREMENT))) {
        op = op(PRE_INCREMENT);
        tokens.next();
      } else if (tokens.curr().eq(op(PRE_DECREMENT))) {
        op = op(PRE_DECREMENT);
        tokens.next();
      } else {
        tokens.prev();
        return null;
      }
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
    } else {
      return null;
    }
    if (tokens.curr() instanceof Identifier) {
      id = (Identifier) tokens.curr();
      tokens.next();
      id.arrSel = ctx.arraySelectorParser.parse(tokens);
      return new IncrementStatement(new Expression(List.of(id, op)));
    } else {
      ctx.errors.tokenIdx = tokens.idx();
      tokens.setIdx(idx);
      return null;
    }
  }
}
