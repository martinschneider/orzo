package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.ENUM;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.EXTENDS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.IMPLEMENTS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.IMPORT;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.INTERFACE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.PACKAGE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.parser.productions.Clazz.JAVA_LANG_ENUM;
import static io.github.martinschneider.orzo.parser.productions.Clazz.JAVA_LANG_OBJECT;
import static io.github.martinschneider.orzo.util.FactoryHelper.defaultConstr;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.parser.productions.ClassMember;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.ArrayList;
import java.util.List;

public class ClassParser implements ProdParser<Clazz> {
  public ParserContext ctx;
  private static final String LOG_NAME = "parse class";

  public ClassParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Clazz parse(TokenList tokens) {
    String name;
    List<Method> methods;
    List<ParallelDeclaration> decls;
    List<ClassMember> members = new ArrayList<>();
    String packageDeclaration = parsePackageDeclaration(tokens);
    ctx.importMap.clear();
    List<Import> imports = parseImports(tokens);
    List<String> interfaces = new ArrayList<>();
    String baseClass = JAVA_LANG_OBJECT;
    Scope scope = ctx.scopeParser.parse(tokens);
    boolean isInterface = false;
    boolean isEnum = false;
    if (tokens.curr() instanceof Keyword) {
      if (tokens.curr().eq(keyword(INTERFACE))) {
        isInterface = true;
      } else if (tokens.curr().eq(keyword(ENUM))) {
        isEnum = true;
      }
      if (tokens.curr().eq(keyword(CLASS)) || isInterface || isEnum) {
        tokens.next();
        if (tokens.curr() instanceof Identifier) {
          name = tokens.curr().val.toString();
        } else {
          name = null;
          ctx.errors.addError(
              LOG_NAME, "missing identifier", new RuntimeException().getStackTrace());
        }
        tokens.next();
        // Parse generic type parameters on class/interface declaration e.g. class Foo<T extends
        // Bound>
        ctx.classTypeParams.clear();
        if (tokens.curr().eq(op(LESS))) {
          tokens.next();
          while (!tokens.curr().eq(op(GREATER)) && !tokens.curr().eq(op(RSHIFT))) {
            if (tokens.curr() instanceof Identifier) {
              String typeVarName = tokens.curr().toString();
              String erasure = "java.lang.Object";
              tokens.next();
              if (tokens.curr().eq(keyword(EXTENDS))) {
                tokens.next();
                // Collect bound type (until , or > or &)
                StringBuilder bound = new StringBuilder();
                while (!tokens.curr().eq(sym(COMMA))
                    && !tokens.curr().eq(op(GREATER))
                    && !tokens.curr().eq(op(RSHIFT))
                    && !tokens.curr().eq(op(BITWISE_AND))) {
                  if (bound.length() > 0 && tokens.curr().toString().equals(".")) {
                    bound.append(".");
                  } else if (bound.length() > 0) {
                    bound.append(".");
                    bound.append(tokens.curr().toString());
                  } else {
                    bound.append(tokens.curr().toString());
                  }
                  tokens.next();
                }
                erasure = ctx.importMap.getOrDefault(bound.toString(), bound.toString());
              }
              ctx.classTypeParams.put(typeVarName, erasure);
            }
            if (tokens.curr().eq(sym(COMMA))) {
              tokens.next();
            } else if (tokens.curr().eq(op(BITWISE_AND))) {
              // Skip additional bounds (e.g. T extends A & B) - use first bound only
              while (!tokens.curr().eq(sym(COMMA)) && !tokens.curr().eq(op(GREATER))) {
                tokens.next();
              }
            }
          }
          tokens.next(); // consume >
        }
        parseInterfaces(tokens, interfaces, packageDeclaration);
        baseClass = parseBaseClass(tokens, isEnum, packageDeclaration);
        if (interfaces.isEmpty()) {
          parseInterfaces(tokens, interfaces, packageDeclaration);
        }
        if (!tokens.curr().eq(sym(LBRACE))) {
          ctx.errors.tokenIdx = tokens.idx();
          ctx.errors.missingExpected(
              LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
          return null;
        }
        tokens.next();
        // set temporary clazz object for the method parser's use
        ctx.currClazz =
            new Clazz(
                packageDeclaration,
                imports,
                scope,
                name,
                isInterface,
                isEnum,
                interfaces,
                baseClass,
                null,
                null,
                null);
        if (isEnum) {
          ctx.enumParser.fqn = ctx.currClazz.fqn();
          members.add(ctx.enumParser.parse(tokens));
          // Continue parsing remaining class body (fields, methods, etc.)
          List<ClassMember> additionalMembers = parseClassBody(tokens, isInterface);
          members.addAll(additionalMembers);
        } else {
          members = parseClassBody(tokens, isInterface);
        }
        if (members == null) {
          ctx.errors.addError(
              LOG_NAME, "missing class body", new RuntimeException().getStackTrace());
        }
        methods = new ArrayList<>();
        decls = new ArrayList<>();
        boolean hasConstr = false;
        for (ClassMember member : members) {
          if (member instanceof Method) {
            Method method = (Method) member;
            hasConstr = hasConstr || method instanceof Constructor;
            methods.add(method);
          } else if (member instanceof ParallelDeclaration) {
            decls.add((ParallelDeclaration) member);
          }
        }
        // TODO: one could argue that this should be handled during code generation rather than
        // parsing
        if (!isInterface && !isEnum && !hasConstr) {
          methods.add(defaultConstr(ctx.currClazz.fqn()));
        }

        if (!tokens.curr().eq(sym(RBRACE))) {
          ctx.errors.tokenIdx = tokens.idx();
          ctx.errors.missingExpected(
              LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
        }
        ctx.currClazz.methods = methods;
        ctx.currClazz.fields = decls;
        tokens.next();
        ctx.currClazz.baseClass = baseClass;
        return ctx.currClazz;
      }
    }
    return null;
  }

  private String parseBaseClass(TokenList tokens, boolean isEnum, String packageDeclaration) {
    if (tokens.curr().eq(keyword(EXTENDS))) {
      tokens.next();
      if (tokens.curr() instanceof Identifier) {
        String baseClass = (((Identifier) tokens.curr()).val.toString());
        tokens.next();
        return resolveClassName(baseClass, packageDeclaration);
      }
    }
    return isEnum ? JAVA_LANG_ENUM : JAVA_LANG_OBJECT;
  }

  private String resolveClassName(String name, String packageDeclaration) {
    if (name.contains(".")) {
      return name;
    }
    String fqn = ctx.importMap.get(name);
    if (fqn != null) {
      return fqn;
    }
    if (packageDeclaration != null && !packageDeclaration.isEmpty()) {
      return packageDeclaration + "." + name;
    }
    return name;
  }

  List<String> parseInterfaces(
      TokenList tokens, List<String> interfaces, String packageDeclaration) {
    if (tokens.curr().eq(keyword(IMPLEMENTS))) {
      tokens.next();
      while (!tokens.curr().eq(keyword(EXTENDS))
          && !(tokens.curr().eq(sym(LBRACE)))
          && !(tokens.curr() instanceof EOF)) {
        if (tokens.curr() instanceof Identifier) {
          interfaces.add(
              resolveClassName(((Identifier) tokens.curr()).val.toString(), packageDeclaration));
          tokens.next();
          // Skip generic type arguments e.g. ProdParser<Clazz>
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
        } else {
          tokens.next();
        }
        if (tokens.curr().equals(sym(COMMA))) {
          tokens.next();
        } else {
          break;
        }
      }
    }
    return interfaces;
  }

  List<ClassMember> parseClassBody(TokenList tokens, boolean isInterface) {
    List<ClassMember> classBody = new ArrayList<>();
    ClassMember member = null;
    int lastIdx = -1;
    int stuckCount = 0;
    while ((member = parserClassMember(tokens, isInterface)) != null) {
      classBody.add(member);
      int currIdx = tokens.idx();
      if (currIdx == lastIdx) {
        stuckCount++;
        if (stuckCount >= 3) {
          System.err.println(
              "[LOOP GUARD] parseClassBody stuck at token idx="
                  + currIdx
                  + " token="
                  + tokens.curr()
                  + " member="
                  + member.getClass().getSimpleName());
          break;
        }
      } else {
        stuckCount = 0;
      }
      lastIdx = currIdx;
    }
    if (!classBody.isEmpty()) {
      return classBody;
    } else {
      return emptyList();
    }
  }

  private ClassMember parserClassMember(TokenList tokens, boolean isInterface) {
    ClassMember member = null;
    if ((member = ctx.methodParser.parse(tokens, isInterface)) != null) {
      return member;
    } else if ((member = ctx.declParser.parse(tokens)) != null) {
      ParallelDeclaration pDecl = (ParallelDeclaration) member;
      for (Declaration decl : pDecl.declarations) {
        decl.isField = true;
      }
      return pDecl;
    }
    return null;
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
        } else if (tokens.curr().eq(op(TIMES))) {
          identifier.append('*');
        } else {
          ctx.errors.addError(
              LOG_NAME,
              "invalid token " + tokens.curr() + " in import",
              new RuntimeException().getStackTrace());
          return new Import(identifier.toString(), isStatic);
        }
        tokens.next();
      }
      String importPath = identifier.toString();
      if (!isStatic) {
        int lastDot = importPath.lastIndexOf('.');
        if (lastDot >= 0) {
          String simpleName = importPath.substring(lastDot + 1);
          if (!simpleName.equals("*")) {
            ctx.importMap.put(simpleName, importPath);
          }
        }
      }
      tokens.next();
      return new Import(importPath, isStatic);
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
              LOG_NAME,
              "invalid token " + tokens.curr() + " in package declaration",
              new RuntimeException().getStackTrace());
          return packageName.toString();
        }
        tokens.next();
      }
      tokens.next();
      return packageName.toString();
    }
    return "";
  }
}
