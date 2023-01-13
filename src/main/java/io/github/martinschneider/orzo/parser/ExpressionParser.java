package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operator.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.POW;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SQRT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.eof;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.INT_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.SYMBOL;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.TokenType;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ExpressionToken;
import io.github.martinschneider.orzo.parser.productions.Identifier;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.Type;

public class ExpressionParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse expression";

  public ExpressionParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  // TODO: simplify and document this
  public Expression parse(TokenList tokens) {
    Expression expr = null;
    if ((expr = ctx.arrayInitParser.parse(tokens)) != null) {
      return expr;
    }
    Type cast = ctx.castParser.parse(tokens);
    List<Token> exprTokens = new ArrayList<>();
    checkNegative(tokens, exprTokens);
    int parenthesis = 0;
    outer:
    {
      while (tokens.curr().isLit()
          || tokens.curr().isId()
          || tokens.curr().isOp()
          || tokens.curr().eq(sym(SQRT))
          || tokens.curr().eq(sym(LPAREN))
          || tokens.curr().eq(sym(RPAREN))
          || isNewKeyword(tokens.curr())) {
        int idx = tokens.idx();
        boolean negative = false;
        if (List.of(sym(LPAREN), op(TIMES), op(DIV), op(POW), op(MOD))
            .contains(tokens.peekPrev())) {
          negative = checkNegative(tokens, exprTokens);
        }
        if (negative) {
          idx = tokens.idx();
        }
        List<Identifier> selectors = new ArrayList<>();
        do {
          idx = tokens.idx();
          MethodCall methodCall = parseMethod(tokens);
          if (methodCall != null) {
            selectors.add(methodCall);
          } else {
            tokens.setIdx(idx);
            if (tokens.curr().eq(sym(LPAREN))) {
              parenthesis--;
            } else if (tokens.curr().eq(sym(RPAREN))) {
              parenthesis++;
            }
            if (parenthesis > 0) {
              break outer;
            }
            Token curr = tokens.curr();
            ConstructorCall constrCall = null;
            if (!curr.eq(eof())) {
              if (curr.isId()) {
                selectors.add(Identifier.of(curr.val));
                tokens.next();
              } else if ((constrCall = ctx.constrCallParser.parse(tokens)) != null) {
                selectors.add(constrCall);
              } else {
                exprTokens.add(curr);
                tokens.next();
                break;
              }
            }
          }
        } while (tokens.curr().eq(sym(DOT)) && tokens.next() != null);
        //if (!selectors.isEmpty()) exprTokens.add(flattenId(selectors));
      }
    }
    return (exprTokens.size() > 0) ? new Expression(postfix(exprTokens), cast) : null;
  }

  private boolean isNewKeyword(Token curr) {
    return (curr.eq(TokenType.KEYWORD, Keyword.NEW.name()));
  }

  private Identifier flattenId(List<Identifier> selectors) {
    Identifier id = selectors.get(0);
    Identifier root = id;
    for (int i = 1; i < selectors.size(); i++) {
      id.next = selectors.get(i);
      id = id.next;
    }
    return root;
  }

  private MethodCall parseMethod(TokenList tokens) {
    if (tokens.curr().isId()) {
      Identifier id = Identifier.of(tokens.curr().val);
      tokens.next();
      ArraySelector sel = ctx.arraySelectorParser.parse(tokens, true);
      if (sel != null) {
        id.arrSel = sel;
      } else {
        tokens.prev();
      }
    }
    MethodCall methodCall = ctx.methodCallParser.parse(tokens, true);
    if (methodCall != null) {
      return methodCall;
    }
    return null;
  }

  private boolean checkNegative(TokenList tokens, List<Token> exprTokens) {
    if (tokens.curr().eq(op(MINUS))) {
      tokens.next();
      if (tokens.curr().isNum()) {
        tokens.curr().changeSign();
        exprTokens.add(tokens.curr());
        tokens.next();
        return true;
      } else if (tokens.curr().isId()) {
        exprTokens.add(sym(LPAREN));
        exprTokens.add(Token.of(INT_LITERAL,"-1"));
        exprTokens.add(op(TIMES));
        exprTokens.add(tokens.curr());
        exprTokens.add(sym(RPAREN));
        tokens.next();
        return true;
      } else {
        ctx.errors.addError(
            LOG_NAME,
            "unexpected symbol "
                + tokens.curr()
                + " after starting \"-\" in expression (expected number literal or identifier)",
            new RuntimeException().getStackTrace());
      }
    }
    return false;
  }

  private boolean isHigerPrec(Operator op, Token sub) {
    return (sub.isOp() && sub.opVal().getPrecedence() >= op.getPrecedence());
  }

  // shunting yard algorithm to transform the token list to postfix notation
  private List<Token> postfix(List<Token> tokens) {
    List<Token> output = new ArrayList<>();
    Deque<Token> stack = new LinkedList<>();
    for (int i = 0; i < tokens.size(); i++) {
    	Token token = tokens.get(i);
      if (token.isOp()) {
        while (!stack.isEmpty() && isHigerPrec(token.opVal(), stack.peek())) {
          output.add(stack.pop());
        }
        stack.push(token);
      } else if (token.eq(SYMBOL, LPAREN)) {
        stack.push(token);
      } else if (token.eq(SYMBOL, RPAREN)) {
        while (!stack.peek().eq(SYMBOL, LPAREN)) {
          output.add(stack.pop());
        }
        stack.pop();
      } else {
        output.add(token);
      }
    }
    while (!stack.isEmpty()) {
      output.add(stack.pop());
    }
    return output;
  }
}
