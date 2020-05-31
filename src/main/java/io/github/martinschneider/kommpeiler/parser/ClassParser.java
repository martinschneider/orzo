package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.CLASS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.IMPORT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.PACKAGE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.STATIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.DOT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Import;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.EOF;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Keyword;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassParser implements ProdParser<Clazz> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse class";

  public ClassParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Clazz parse(TokenList tokens) {
    Identifier name;
    List<Method> body;
    String packageDeclaration = parsePackageDeclaration(tokens);
    List<Import> imports = parseImports(tokens);
    Scope scope = ctx.scopeParser.parse(tokens);
    if (tokens.curr() instanceof Keyword) {
      if (tokens.curr().eq(keyword(CLASS))) {
        tokens.next();
        if (tokens.curr() instanceof Identifier) {
          name = (Identifier) tokens.curr();
        } else {
          name = null;
          ctx.errors.addError(LOG_NAME, "missing identifier");
        }
        tokens.next();
        if (!tokens.curr().eq(sym(LBRACE))) {
          ctx.errors.missingExpected(LOG_NAME, sym(LBRACE), tokens);
        }
        tokens.next();
        body = parseClassBody(tokens);
        if (body == null) {
          ctx.errors.addError(LOG_NAME, "missing class body");
        }
        if (!tokens.curr().eq(sym(RBRACE))) {
          ctx.errors.missingExpected(LOG_NAME, sym(RBRACE), tokens);
        }
        tokens.next();
        return new Clazz(packageDeclaration, imports, scope, name, body);
      }
    }
    return null;
  }

  List<Method> parseClassBody(TokenList tokens) {
    List<Method> classBody = new ArrayList<>();
    Method method;
    while ((method = ctx.methodParser.parse(tokens)) != null) {
      classBody.add(method);
    }
    if (!classBody.isEmpty()) {
      return classBody;
    } else {
      return Collections.emptyList();
    }
  }

  List<Import> parseImports(TokenList tokens) {
    List<Import> imports = new ArrayList<>();
    Import importStmt;
    while ((importStmt = parseImport(tokens)) != null) {
      imports.add(importStmt);
    }
    return imports;
  }

  Import parseImport(TokenList tokens) {
    boolean isStatic = false;
    if (tokens.curr().eq(keyword(IMPORT))) {
      tokens.next();
      if (tokens.curr().eq(keyword(STATIC))) {
        isStatic = true;
        tokens.next();
      }
      StringBuilder identifier = new StringBuilder();
      while (!tokens.curr().eq(sym(SEMICOLON)) && !(tokens.curr() instanceof EOF)) {
        if (tokens.curr().eq(sym(DOT))) {
          identifier.append('.');
        } else if (tokens.curr() instanceof Identifier) {
          identifier.append(tokens.curr().getValue());
        } else {
          ctx.errors.addError(LOG_NAME, "invalid token " + tokens.curr() + " in import");
          return new Import(identifier.toString(), isStatic);
        }
        tokens.next();
      }
      tokens.next();
      return new Import(identifier.toString(), isStatic);
    }
    return null;
  }

  String parsePackageDeclaration(TokenList tokens) {
    if (tokens.curr().eq(keyword(PACKAGE))) {
      tokens.next();
      StringBuilder packageName = new StringBuilder();
      while (!tokens.curr().eq(sym(SEMICOLON)) && !(tokens.curr() instanceof EOF)) {
        if (tokens.curr().eq(sym(DOT))) {
          packageName.append('.');
        } else if (tokens.curr() instanceof Identifier) {
          packageName.append(tokens.curr().getValue());
        } else {
          ctx.errors.addError(
              LOG_NAME, "invalid token " + tokens.curr() + " in package declaration");
          return packageName.toString();
        }
        tokens.next();
      }
      tokens.next();
      return packageName.toString();
    }
    return null;
  }
}
