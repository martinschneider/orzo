package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.STATIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import io.github.martinschneider.kommpeiler.scanner.tokens.Type;
import java.util.ArrayList;
import java.util.List;

public class MethodParser implements ProdParser<Method> {
  ParserContext ctx;

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
    if (tokens.curr() instanceof Type) {
      type = ((Type) tokens.curr()).getName();
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
      return new Method(scope, type, name, arguments, body);
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
      type = ((Type) tokens.curr()).getName();
      if (tokens.next().eq(sym(LBRAK))) {
        if (tokens.next().eq(sym(RBRAK))) {
          type = "[" + type;
        } else {
          ctx.errors.addParserError("missing ] in type declaration");
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
