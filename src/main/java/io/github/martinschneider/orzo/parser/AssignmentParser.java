package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.ArrayList;
import java.util.List;

public class AssignmentParser implements ProdParser<Assignment> {
  private ParserContext ctx;

  public AssignmentParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Assignment parse(TokenList tokens) {
    List<Identifier> left = new ArrayList<>();
    List<Expression> right = new ArrayList<>();
    int idx = tokens.idx();
    while (tokens.curr() instanceof Identifier || tokens.curr().eq(sym(COMMA))) {
      if (tokens.curr() instanceof Identifier) {
        Identifier id = (Identifier) tokens.curr();
        tokens.next();
        id.arrSel = ctx.arraySelectorParser.parse(tokens);
        left.add(id);
      } else {
        tokens.next();
      }
    }
    if (tokens.curr() instanceof Operator) {
      if (tokens.curr().eq(op(ASSIGN))) {
        tokens.next();
      } else if (tokens.curr().eq(op(POST_INCREMENT)) || tokens.curr().eq(op(POST_DECREMENT))) {
        // see post increment parser
        tokens.setIdx(idx);
        return null;
      } else if (tokens.curr().eq(op(PLUS_ASSIGN))) {
        tokens.insert(op(PLUS));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(MINUS_ASSIGN))) {
        tokens.insert(op(MINUS));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(TIMES_ASSIGN))) {
        tokens.insert(op(TIMES));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(DIV_ASSIGN))) {
        tokens.insert(op(DIV));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(MOD_ASSIGN))) {
        tokens.insert(op(MOD));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(LSHIFT_ASSIGN))) {
        tokens.insert(op(LSHIFT));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(RSHIFT_ASSIGN))) {
        tokens.insert(op(RSHIFT));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(RSHIFTU_ASSIGN))) {
        tokens.insert(op(RSHIFTU));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_AND_ASSIGN))) {
        tokens.insert(op(BITWISE_AND));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_OR_ASSIGN))) {
        tokens.insert(op(BITWISE_OR));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_XOR_ASSIGN))) {
        tokens.insert(op(BITWISE_XOR));
        tokens.insert(left.get(0));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else {
        tokens.prev();
        return null;
      }
      Expression expression = null;
      while ((expression = ctx.exprParser.parse(tokens)) != null || tokens.curr().eq(sym(COMMA))) {
        if (expression != null) {
          right.add(expression);
        } else {
          tokens.next();
        }
      }
      Assignment assignment = new Assignment(left, right);
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
      return assignment;
    }
    tokens.setIdx(idx);
    return null;
  }
}
