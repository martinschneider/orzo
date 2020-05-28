package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.DOT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.eof;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.error.ErrorType;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.ArrayList;
import java.util.List;

public class MethodCallParser implements ProdParser<MethodCall> {
  private ParserContext ctx;

  public MethodCallParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public MethodCall parse(TokenList tokens) {
    return parse(tokens, false);
  }

  List<Expression> parseArgs(TokenList tokens) {
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
        ctx.errors.addError(") expected", ErrorType.PARSER);
      }
      tokens.next();
      return parameters;
    }
    // else
    return null;
  }

  // By default a method call production matches the trailing semicolon. In situations where this is
  // not wanted (for example, inside an expression), we can call this method with
  // saveSemicolon=true.
  public MethodCall parse(TokenList tokens, boolean saveSemicolon) {
    List<Expression> parameters;
    StringBuilder name = new StringBuilder();
    if (tokens.curr() instanceof Identifier) {
      int idx = tokens.idx();
      do {
        name.append(tokens.curr());
        tokens.next();
        name.append('.');
      } while ((tokens.curr().eq(sym(DOT)) && !tokens.next().eq(eof())));
      name.deleteCharAt(name.length() - 1);
      parameters = parseArgs(tokens);
      if (tokens.curr().eq(sym(SEMICOLON)) && !saveSemicolon) {
        tokens.next();
      }
      if (parameters == null) {
        tokens.setIdx(idx);
        return null;
      }
      return new MethodCall(id(name.toString()), parameters);
    }
    return null;
  }
}
