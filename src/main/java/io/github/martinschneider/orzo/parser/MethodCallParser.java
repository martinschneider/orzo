package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbol.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.eof;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import java.util.ArrayList;
import java.util.List;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Identifier;
import io.github.martinschneider.orzo.parser.productions.MethodCall;

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
    if (tokens.curr().isId()) {
      Identifier idToken = Identifier.of(tokens.curr().val);
      int idx = tokens.idx();
      do {
        name.append(tokens.curr());
        tokens.next();
        name.append('.');
      } while ((tokens.curr().eq(sym(DOT)) && !tokens.next().eq(eof())));
      name.deleteCharAt(name.length() - 1);
      parameters = parseArgs(tokens);
      ArraySelector arrSel = ctx.arraySelectorParser.parse(tokens);
      if (tokens.curr().eq(sym(SEMICOLON)) && !saveSemicolon) {
        tokens.next();
      }
      if (parameters == null) {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
      return new MethodCall(name.toString(), parameters, arrSel);//.wLoc(idToken.loc);
    }
    return null;
  }
}
