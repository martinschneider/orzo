package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.eof;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.util.ArrayList;
import java.util.List;

public class MethodCallParser implements ProdParser<MethodCall> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse method call";

  public MethodCallParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public MethodCall parse(TokenList tokens) {
    return parse(tokens, false);
  }

  public List<Expression> parseArgs(TokenList tokens) {
    List<Expression> parameters = new ArrayList<>();
    if (tokens.curr().eq(sym(LPAREN))) {
      tokens.next();
      Expression factor;
      if ((factor = ctx.exprParser.parse(tokens)) != null) {
        parameters.add(factor);
      }
      while (tokens.curr().eq(sym(COMMA))) {
        tokens.next();
        if ((factor = ctx.exprParser.parse(tokens)) != null) {
          parameters.add(factor);
        }
        // TODO: else
      }
      if (!tokens.curr().eq(sym(RPAREN))) {
        tokens.next(sym(RBRACE));
        ctx.errors.missingExpected(
            LOG_NAME, sym(RPAREN), tokens, new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next();
      return parameters;
    }
    // else
    return null;
  }

  // By default a method call production matches the trailing semicolon. In
  // situations where this is
  // not wanted (for example, inside an expression), we can call this method with
  // saveSemicolon=true.
  public MethodCall parse(TokenList tokens, boolean saveSemicolon) {
    ctx.floorParser.parse(tokens);
    ctx.sqrtParser.parse(tokens);
    List<Expression> parameters;
    StringBuilder name = new StringBuilder();
    if (tokens.curr() instanceof Identifier) {
      Identifier idToken = (Identifier) tokens.curr();
      int idx = tokens.idx();
      do {
        name.append(tokens.curr());
        tokens.next();
        name.append('.');
        // a dotted name only continues with another name, not e.g. "class" in "Foo.class.x()"
      } while (tokens.curr().eq(sym(DOT))
          && (tokens.peekNext() instanceof Identifier || tokens.peekNext() instanceof Type)
          && !tokens.next().eq(eof()));
      name.deleteCharAt(name.length() - 1);
      parameters = parseArgs(tokens);
      ArraySelector arrSel = ctx.arraySelectorParser.parse(tokens);
      if (parameters == null) {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
      MethodCall result = new MethodCall(name.toString(), parameters, arrSel);
      MethodCall last = result;
      // Support chained calls as a standalone statement, e.g. "sb.append(x).append(y);".
      // The dotted-name loop above only handles a dotted receiver/field chain ending in a
      // single call; here we keep following ".name(args)" segments after the first call.
      while (tokens.curr().eq(sym(DOT))) {
        int chainIdx = tokens.idx();
        tokens.next(); // consume '.'
        if (!(tokens.curr() instanceof Identifier)) {
          tokens.setIdx(chainIdx);
          break;
        }
        String nextName = tokens.curr().val.toString();
        tokens.next();
        List<Expression> nextParams = parseArgs(tokens);
        if (nextParams == null) {
          tokens.setIdx(chainIdx);
          break;
        }
        ArraySelector nextArrSel = ctx.arraySelectorParser.parse(tokens);
        MethodCall next = new MethodCall(nextName, nextParams, nextArrSel);
        last.next = next;
        last = next;
      }
      if (tokens.curr().eq(sym(SEMICOLON)) && !saveSemicolon) {
        tokens.next();
      }
      return result;
    }
    return null;
  }
}
