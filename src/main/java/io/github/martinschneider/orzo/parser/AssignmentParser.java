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
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;

public class AssignmentParser implements ProdParser<Assignment> {
  private ParserContext ctx;

  public AssignmentParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Assignment parse(TokenList tokens) {
    Identifier left;
    Expression right;
    int idx = tokens.idx();
    if (tokens.curr() instanceof Identifier) {
      left = (Identifier) tokens.curr();
      tokens.next();
      left.arrSel = ctx.arraySelectorParser.parse(tokens);
    } else {
      return null;
    }
    if (tokens.curr() instanceof Operator) {
      if (tokens.curr().eq(op(ASSIGN))) {
        tokens.next();
      } else if (tokens.curr().eq(op(POST_INCREMENT)) || tokens.curr().eq(op(POST_DECREMENT))) {
        tokens.prev();
      } else if (tokens.curr().eq(op(PLUS_ASSIGN))) {
        tokens.insert(op(PLUS));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(MINUS_ASSIGN))) {
        tokens.insert(op(MINUS));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(TIMES_ASSIGN))) {
        tokens.insert(op(TIMES));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(DIV_ASSIGN))) {
        tokens.insert(op(DIV));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(MOD_ASSIGN))) {
        tokens.insert(op(MOD));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(LSHIFT_ASSIGN))) {
        tokens.insert(op(LSHIFT));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(RSHIFT_ASSIGN))) {
        tokens.insert(op(RSHIFT));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(RSHIFTU_ASSIGN))) {
        tokens.insert(op(RSHIFTU));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_AND_ASSIGN))) {
        tokens.insert(op(BITWISE_AND));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_OR_ASSIGN))) {
        tokens.insert(op(BITWISE_OR));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_XOR_ASSIGN))) {
        tokens.insert(op(BITWISE_XOR));
        tokens.insert(left);
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else {
        tokens.prev();
        return null;
      }
      if ((right = ctx.exprParser.parse(tokens)) == null) {
        tokens.prev();
      } else {
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
        Assignment assignment = new Assignment(left, right);
        return assignment;
      }
    } else {
      tokens.setIdx(idx);
      return null;
    }
    return null;
  }
}
