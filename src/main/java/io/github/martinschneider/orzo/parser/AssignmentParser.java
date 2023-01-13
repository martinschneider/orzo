package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operator.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_AND_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_OR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_XOR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.DIV_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MINUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MOD_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.PLUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFTU_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.TIMES_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.*;

import java.util.ArrayList;
import java.util.List;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Identifier;

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
    while (tokens.curr().isId() || tokens.curr().eq(sym(COMMA))) {
      if (tokens.curr().isId()) {
        Identifier id = Identifier.of(tokens.curr().val);
        Identifier root = id;
        while (tokens.peekNext().eq(sym(DOT))) {
          tokens.next();
          if (tokens.peekNext().isId()) {
            Identifier next = Identifier.of(tokens.next().val);
            id.next = next;
            id = id.next;
            tokens.remove(tokens.idx());
            tokens.remove(tokens.idx() - 1);
            tokens.prev();
            tokens.prev();
          }
        }
        tokens.next();
        id = root;
        id.arrSel = ctx.arraySelectorParser.parse(tokens);
        left.add(id);
      } else {
        tokens.next();
      }
    }
    if (tokens.curr().isOp()) {
      if (tokens.curr().eq(op(ASSIGN))) {
        tokens.next();
      } else if (tokens.curr().eq(op(POST_INCREMENT)) || tokens.curr().eq(op(POST_DECREMENT))) {
        // see post increment parser
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      } else if (tokens.curr().eq(op(PLUS_ASSIGN))) {
        tokens.insert(op(PLUS));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(MINUS_ASSIGN))) {
        tokens.insert(op(MINUS));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(TIMES_ASSIGN))) {
        tokens.insert(op(TIMES));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(DIV_ASSIGN))) {
        tokens.insert(op(DIV));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(MOD_ASSIGN))) {
        tokens.insert(op(MOD));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(LSHIFT_ASSIGN))) {
        tokens.insert(op(LSHIFT));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(RSHIFT_ASSIGN))) {
        tokens.insert(op(RSHIFT));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(RSHIFTU_ASSIGN))) {
        tokens.insert(op(RSHIFTU));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_AND_ASSIGN))) {
        tokens.insert(op(BITWISE_AND));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_OR_ASSIGN))) {
        tokens.insert(op(BITWISE_OR));
        tokens.insert(Token.of(ID, left.get(0).name));
        tokens.insert(op(ASSIGN));
        tokens.next();
      } else if (tokens.curr().eq(op(BITWISE_XOR_ASSIGN))) {
        tokens.insert(op(BITWISE_XOR));
        tokens.insert(Token.of(ID, left.get(0).name));
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
    ctx.errors.tokenIdx = tokens.idx();
    tokens.setIdx(idx);
    return null;
  }
}
