package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Num;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;

public class ExpressionParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse expression";

  public ExpressionParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Expression parse(TokenList tokens) {
    Expression expression = new Expression();
    boolean negative = false;
    if (tokens.curr().eq(op(MINUS))) {
      negative = true;
      tokens.next();
      if (tokens.curr() instanceof Num) {
        Num number = (Num) tokens.curr();
        if (negative) {
          number.changeSign();
        }
        expression.addToken(tokens.curr());
        tokens.next();
      } else if (tokens.curr() instanceof Identifier) {
        expression.addToken(sym(LPAREN));
        expression.addToken(integer(-1));
        expression.addToken(sym(RPAREN));
        expression.addToken(op(TIMES));
        expression.addToken(tokens.curr());
      } else {
        ctx.errors.addError(
            LOG_NAME,
            "unexpected symbol "
                + tokens.curr()
                + " after starting \"-\" in expression (expected number literal or identifier)");
        return null;
      }
    }
    int parenthesis = 0;
    while (tokens.curr() instanceof Num
        || tokens.curr() instanceof Str
        || tokens.curr() instanceof Identifier
        || tokens.curr() instanceof Operator
        || tokens.curr().eq(sym(LPAREN))
        || tokens.curr().eq(sym(RPAREN))) {
      int idx = tokens.idx();
      if (tokens.curr() instanceof Identifier) {
        Identifier id = ((Identifier) tokens.curr());
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
        expression.addToken(methodCall);
      } else {
        tokens.setIdx(idx);
        if (tokens.curr().eq(sym(LPAREN))) {
          parenthesis--;
        } else if (tokens.curr().eq(sym(RPAREN))) {
          parenthesis++;
        }
        if (parenthesis > 0) {
          break;
        }
        expression.addToken(tokens.curr());
        tokens.next();
      }
    }
    return (expression.size() > 0) ? expression : null;
  }
}
