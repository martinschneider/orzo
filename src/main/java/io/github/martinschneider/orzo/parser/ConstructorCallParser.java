package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.List;

public class ConstructorCallParser implements ProdParser<ConstructorCall> {

  private ParserContext ctx;

  private static final String LOG_NAME = "parse constructor call";

  public ConstructorCallParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ConstructorCall parse(TokenList tokens) {
    return parse(tokens, false);
  }

  public ConstructorCall parse(TokenList tokens, boolean saveSemicolon) {
    int idx = tokens.idx();
    if (!(tokens.curr() instanceof Keyword && ((Keyword) tokens.curr()).eq(keyword("new")))) {
      return null;
    }
    tokens.next();
    String typeName = null;
    if (tokens.curr() instanceof Identifier) {
      StringBuilder fqn = new StringBuilder(((Identifier) tokens.curr()).id());
      tokens.next();
      // Fully-qualified type name, e.g. "new java.util.HashMap<>()". The last segment of a
      // built-in-sounding name (e.g. "java.lang.String") lexes as a Type token.
      while (tokens.curr().eq(sym(DOT))
          && (tokens.peekNext() instanceof Identifier || tokens.peekNext() instanceof Type)) {
        tokens.next();
        Token seg = tokens.curr();
        fqn.append('.').append(seg instanceof Type ? ((Type) seg).name : seg.val.toString());
        tokens.next();
      }
      typeName = fqn.toString();
    } else if (tokens.curr() instanceof Type) {
      typeName = ((Type) tokens.curr()).name;
      tokens.next();
    } else {
      ctx.errors.addError(
          LOG_NAME,
          "identifier or type expected after \"new\"",
          new RuntimeException().getStackTrace());
      tokens.prev();
      return null;
    }
    // Skip generic type arguments e.g. <T>, <K, V>, <>, <List<T>>
    if (tokens.curr().eq(op(LESS))) {
      int depth = 1;
      while (depth > 0) {
        tokens.next();
        if (tokens.curr().eq(op(LESS))) depth++;
        else if (tokens.curr().eq(op(GREATER))) depth--;
        else if (tokens.curr().eq(op(RSHIFT))) depth = Math.max(0, depth - 2);
        else if (tokens.curr().eq(op(RSHIFTU))) depth = Math.max(0, depth - 3);
      }
      tokens.next();
    }
    List<Expression> args = ctx.methodCallParser.parseArgs(tokens);
    if (args != null) {
      ConstructorCall result = new ConstructorCall(typeName, args);
      // Support chained calls as a standalone statement, e.g. "new Foo().bar().baz();". Only
      // when called directly as a statement (!saveSemicolon) — an embedded-expression caller
      // (saveSemicolon=true) already processes the following ".method()" tokens itself.
      if (!saveSemicolon) {
        Identifier last = result;
        while (tokens.curr().eq(sym(DOT))) {
          int chainIdx = tokens.idx();
          tokens.next(); // consume '.'
          if (!(tokens.curr() instanceof Identifier)) {
            tokens.setIdx(chainIdx);
            break;
          }
          String nextName = tokens.curr().val.toString();
          tokens.next();
          List<Expression> nextParams = ctx.methodCallParser.parseArgs(tokens);
          if (nextParams == null) {
            tokens.setIdx(chainIdx);
            break;
          }
          io.github.martinschneider.orzo.parser.productions.MethodCall next =
              new io.github.martinschneider.orzo.parser.productions.MethodCall(
                  nextName, nextParams);
          last.next = next;
          last = next;
        }
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
      }
      return result;
    } else {
      ctx.errors.tokenIdx = tokens.idx();
      tokens.setIdx(idx);
      return null;
    }
  }
}
