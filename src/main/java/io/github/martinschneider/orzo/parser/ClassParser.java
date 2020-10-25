package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.IMPORT;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.PACKAGE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.parser.productions.ClassMember;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.ArrayList;
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
    List<Method> methods;
    List<ParallelDeclaration> decls;
    List<ClassMember> members;
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
        // set temporary clazz object for the method parser's use
        ctx.currClazz = new Clazz(packageDeclaration, imports, scope, name, null, null);
        members = parseClassBody(tokens);
        if (members == null) {
          ctx.errors.addError(LOG_NAME, "missing class body");
        }
        methods = new ArrayList<>();
        decls = new ArrayList<>();
        for (ClassMember member : members) {
          if (member instanceof Method) {
            methods.add((Method) member);
          } else if (member instanceof ParallelDeclaration) {
            decls.add((ParallelDeclaration) member);
          }
        }
        if (!tokens.curr().eq(sym(RBRACE))) {
          ctx.errors.missingExpected(LOG_NAME, sym(RBRACE), tokens);
        }
        tokens.next();
        ctx.currClazz.methods = methods;
        ctx.currClazz.fields = decls;
        return ctx.currClazz;
      }
    }
    return null;
  }

  List<ClassMember> parseClassBody(TokenList tokens) {
    List<ClassMember> classBody = new ArrayList<>();
    ClassMember member = null;
    do {
      if ((member = ctx.methodParser.parse(tokens)) != null) {
        classBody.add(member);
      } else if ((member = ctx.declParser.parse(tokens)) != null) {
        ParallelDeclaration pDecl = (ParallelDeclaration) member;
        for (Declaration decl : pDecl.declarations) {
          decl.isField = true;
        }
        classBody.add(pDecl);
      }
    } while (member != null);
    if (!classBody.isEmpty()) {
      return classBody;
    } else {
      return emptyList();
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
          identifier.append(tokens.curr().val);
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
          packageName.append(tokens.curr().val);
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
