package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class MethodParser implements ProdParser<Method> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse method";

  public MethodParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Method parse(TokenList tokens) {
    int idx = tokens.idx();
    Scope scope = scope(DEFAULT);
    String type = null;
    Identifier name;
    List<Argument> arguments = new ArrayList<>();
    List<Statement> body;
    if (tokens.curr() instanceof Scope) {
      scope = (Scope) tokens.curr();
      tokens.next();
    }
    if (tokens.curr().eq(keyword(STATIC))) {
      // TODO: handle static (for now we just ignore it)
      tokens.next();
    }
    if (tokens.curr().eq(keyword(FINAL))) {
      // TODO: handle final (for now we just ignore it)
      tokens.next();
    }
    if (tokens.curr() instanceof Type) {
      type = ((Type) tokens.curr()).name;
      tokens.next();
    }
    if (tokens.curr() instanceof Identifier) {
      name = (Identifier) tokens.curr();
    } else {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    if (!tokens.curr().eq(sym(LPAREN))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    arguments = parseArgs(tokens);
    if (!tokens.curr().eq(sym(RPAREN))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    if (!tokens.curr().eq(sym(LBRACE))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    body = ctx.stmtParser.parseStmtSeq(tokens);
    if (body == null) {
      body = new ArrayList<>();
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    if (type != null && name != null && body != null) {
      if (arguments == null) {
        arguments = new ArrayList<>();
      }
      String fqn = (ctx.currClazz == null) ? null : ctx.currClazz.fqn();
      return new Method(fqn, scope, type, name, arguments, body);
    }
    return null;
  }

  List<Argument> parseArgs(TokenList tokens) {
    List<Argument> arguments = new ArrayList<>();
    while (!tokens.curr().eq(sym(RPAREN))) {
      String type = null;
      Identifier name = null;
      if (!(tokens.curr() instanceof Type)) {
        break;
      }
      type = ((Type) tokens.curr()).name;
      if (tokens.next().eq(sym(LBRAK))) {
        if (tokens.next().eq(sym(RBRAK))) {
          type = "[" + type;
        } else {
          ctx.errors.missingExpected(LOG_NAME, sym(RBRAK), tokens);
          tokens.next();
          break;
        }
      } else {
        tokens.prev();
      }
      if (tokens.next() instanceof Identifier) {
        name = (Identifier) tokens.curr();
      }
      if (type != null && name != null) {
        arguments.add(new Argument(type, name));
      }
      if (tokens.next().eq(sym(COMMA))) {
        tokens.next();
      } else {
        break;
      }
    }
    return arguments.isEmpty() ? null : arguments;
  }
}
