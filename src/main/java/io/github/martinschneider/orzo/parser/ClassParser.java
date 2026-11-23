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
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.parser.productions.Clazz.JAVA_LANG_ENUM;
import static io.github.martinschneider.orzo.parser.productions.Clazz.JAVA_LANG_OBJECT;
import static io.github.martinschneider.orzo.util.FactoryHelper.defaultConstr;
import static io.github.martinschneider.orzo.util.FactoryHelper.enumMethods;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.ClassMember;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassParser implements ProdParser<Clazz> {
  public ParserContext ctx;
  private static final String LOG_NAME = "parse class";

  public ClassParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Clazz parse(TokenList tokens) {
    String packageDeclaration = parsePackageDeclaration(tokens);
    ctx.importMap.clear();
    ctx.onDemandImports.clear();
    List<Import> imports = parseImports(tokens);
    Scope scope = ModifiersParser.scope(ctx.modifiersParser.parse(tokens));
    boolean isInterface = false;
    boolean isEnum = false;
    if (tokens.curr() instanceof Keyword) {
      if (tokens.curr().eq(keyword(INTERFACE))) {
        isInterface = true;
      } else if (tokens.curr().eq(keyword(ENUM))) {
        isEnum = true;
      }
      if (tokens.curr().eq(keyword(CLASS)) || isInterface || isEnum) {
        return parseClassDeclaration(
            tokens, packageDeclaration, imports, scope, isInterface, isEnum, null);
      }
    }
    return null;
  }

  // "class Name ... { ... }" (the current token is the class/interface/enum keyword)
  private Clazz parseClassDeclaration(
      TokenList tokens,
      String packageDeclaration,
      List<Import> imports,
      Scope scope,
      boolean isInterface,
      boolean isEnum,
      String outerName) {
    String name;
    List<Method> methods;
    List<ParallelDeclaration> decls;
    List<ClassMember> members = new ArrayList<>();
    List<String> interfaces = new ArrayList<>();
    String baseClass = JAVA_LANG_OBJECT;
    tokens.next();
    if (tokens.curr() instanceof Identifier) {
      name = tokens.curr().val.toString();
    } else {
      name = null;
      ctx.errors.addError(LOG_NAME, "missing identifier", new RuntimeException().getStackTrace());
    }
    tokens.next();
    // Parse generic type parameters on class/interface declaration e.g. class Foo<T extends
    // Bound>
    ctx.classTypeParams.clear();
    if (tokens.curr().eq(op(LESS))) {
      tokens.next();
      while (!tokens.curr().eq(op(GREATER)) && !tokens.curr().eq(op(RSHIFT))) {
        if (!(tokens.curr() instanceof Identifier)) {
          // e.g. an annotation, "class C<@A T>" (it used to loop forever here)
          ctx.errors.addError(
              LOG_NAME,
              "expected a type parameter but found " + tokens.curr().display(),
              new RuntimeException().getStackTrace());
          while (!endsTypeParameters(tokens.curr())) {
            tokens.next();
          }
          break;
        } else {
          String typeVarName = tokens.curr().toString();
          String erasure = "java.lang.Object";
          tokens.next();
          if (tokens.curr().eq(keyword(EXTENDS))) {
            tokens.next();
            // Collect bound type (until , or > or &)
            StringBuilder bound = new StringBuilder();
            while (!tokens.curr().eq(sym(COMMA))
                && !endsTypeParameters(tokens.curr())
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
          while (!tokens.curr().eq(sym(COMMA)) && !endsTypeParameters(tokens.curr())) {
            tokens.next();
          }
        }
      }
      if (tokens.curr().eq(op(GREATER)) || tokens.curr().eq(op(RSHIFT))) {
        tokens.next(); // consume >
      }
    }
    parseInterfaces(tokens, interfaces, packageDeclaration);
    baseClass = parseBaseClass(tokens, isEnum, isInterface, interfaces, packageDeclaration);
    if (interfaces.isEmpty()) {
      parseInterfaces(tokens, interfaces, packageDeclaration);
    }
    if (!tokens.curr().eq(sym(LBRACE))) {
      ctx.errors.missingExpected(
          LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
      return null;
    }
    tokens.next();
    registerNestedClassNames(tokens, packageDeclaration, outerName, name);
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
    ctx.currClazz.outerName = outerName;
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
      ctx.errors.addError(LOG_NAME, "missing class body", new RuntimeException().getStackTrace());
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
    if (!isInterface && !isEnum && !hasConstr) {
      methods.add(defaultConstr(ctx.currClazz.fqn()));
    }
    if (isEnum) {
      methods.addAll(enumMethods(ctx.currClazz.fqn()));
    }

    if (!tokens.curr().eq(sym(RBRACE))) {
      ctx.errors.missingExpected(
          LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
    }
    ctx.currClazz.methods = methods;
    ctx.currClazz.fields = decls;
    tokens.next();
    ctx.currClazz.baseClass = baseClass;
    return ctx.currClazz;
  }

  // "extends Foo" (an interface's "extends A, B" are its superinterfaces)
  private String parseBaseClass(
      TokenList tokens,
      boolean isEnum,
      boolean isInterface,
      List<String> interfaces,
      String packageDeclaration) {
    if (tokens.curr().eq(keyword(EXTENDS))) {
      tokens.next();
      if (isInterface) {
        parseSupertypes(tokens, interfaces, packageDeclaration);
      } else {
        String baseClass = parseSupertype(tokens, packageDeclaration);
        if (baseClass != null) {
          return baseClass;
        }
      }
    }
    return isEnum ? JAVA_LANG_ENUM : JAVA_LANG_OBJECT;
  }

  // A supertype, e.g. "Foo" or "java.util.AbstractList<T>" (the type arguments are erased), or null
  // if there is none
  private String parseSupertype(TokenList tokens, String packageDeclaration) {
    if (!(tokens.curr() instanceof Identifier)) {
      return null;
    }
    StringBuilder name = new StringBuilder(((Identifier) tokens.curr()).id());
    tokens.next();
    while (tokens.curr().eq(sym(DOT))
        && (tokens.peekNext() instanceof Identifier || tokens.peekNext() instanceof Type)) {
      Token segment = tokens.next();
      name.append('.').append((segment instanceof Type) ? ((Type) segment).name : segment.val);
      tokens.next();
    }
    if (tokens.curr().eq(op(LESS))) {
      ctx.typeArgsParser.parse(tokens);
    }
    String type = name.toString();
    if (type.contains(".")) {
      return ctx.typeArgsParser.resolveQualified(type);
    }
    return resolveClassName(type, packageDeclaration);
  }

  // a comma-separated list of supertypes, e.g. "A, B<T>"
  private void parseSupertypes(
      TokenList tokens, List<String> supertypes, String packageDeclaration) {
    String supertype = parseSupertype(tokens, packageDeclaration);
    while (supertype != null) {
      supertypes.add(supertype);
      if (!tokens.curr().eq(sym(COMMA))) {
        return;
      }
      tokens.next();
      supertype = parseSupertype(tokens, packageDeclaration);
    }
  }

  private String resolveClassName(String name, String packageDeclaration) {
    if (name.contains(".")) {
      return name;
    }
    String fqn = ctx.importMap.get(name);
    if (fqn != null) {
      return fqn;
    }
    String samePackage =
        (packageDeclaration != null && !packageDeclaration.isEmpty())
            ? packageDeclaration + "." + name
            : name;
    if (isLoadable(samePackage)) {
      return samePackage;
    }
    // a class of an on-demand import (e.g. "extends PushbackReader" with "import java.io.*;") or
    // of java.lang (e.g. "implements Comparable<T>")
    for (String pkg : ctx.onDemandImports) {
      if (isLoadable(pkg + "." + name)) {
        return pkg + "." + name;
      }
    }
    if (isLoadable("java.lang." + name)) {
      return "java.lang." + name;
    }
    return samePackage;
  }

  private static boolean isLoadable(String className) {
    try {
      Class.forName(className, false, ClassParser.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException | LinkageError e) {
      return false;
    }
  }

  // "implements A, B<T>"
  List<String> parseInterfaces(
      TokenList tokens, List<String> interfaces, String packageDeclaration) {
    if (tokens.curr().eq(keyword(IMPLEMENTS))) {
      tokens.next();
      parseSupertypes(tokens, interfaces, packageDeclaration);
    }
    return interfaces;
  }

  // Makes the class body's nested classes (declared anywhere in it) known by their simple names,
  // e.g. "Frame" -> "a.b.Outer$Frame" (the current token is the first one of the class body)
  private void registerNestedClassNames(
      TokenList tokens, String packageDeclaration, String outerName, String name) {
    String binaryName =
        ((packageDeclaration != null && !packageDeclaration.isEmpty())
                ? packageDeclaration + "."
                : "")
            + (outerName != null ? outerName + "$" : "")
            + name;
    int depth = 0;
    for (int i = tokens.idx(); i < tokens.list().size() && depth >= 0; i++) {
      Token token = tokens.get(i);
      if (token.eq(sym(LBRACE))) {
        depth++;
      } else if (token.eq(sym(RBRACE))) {
        depth--;
      } else if (depth == 0
          && (token.eq(keyword(CLASS)) || token.eq(keyword(INTERFACE)) || token.eq(keyword(ENUM)))
          && i + 1 < tokens.list().size()
          && tokens.get(i + 1) instanceof Identifier) {
        String nestedName = tokens.get(i + 1).val.toString();
        ctx.importMap.put(nestedName, binaryName + "$" + nestedName);
      }
    }
  }

  // Error recovery: continues after the next ';' or the next "{ ... }" block (e.g. a method body)
  private void skipMember(TokenList tokens, int start) {
    if (tokens.idx() <= start) {
      tokens.setIdx(start);
      tokens.next();
    }
    int depth = 0;
    while (!(tokens.curr() instanceof EOF)) {
      if (tokens.curr().eq(sym(LBRACE))) {
        depth++;
      } else if (tokens.curr().eq(sym(RBRACE))) {
        if (depth == 0) {
          return; // the end of the class body
        }
        depth--;
        if (depth == 0) {
          tokens.next();
          return;
        }
      } else if (depth == 0 && tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
        return;
      }
      tokens.next();
    }
  }

  // A static nested class, e.g. "private static final class Frame { ... }": parsed into a separate
  // class "Outer$Frame" (see ParserContext.nestedClazzes)
  private boolean parseNestedClass(TokenList tokens) {
    int idx = tokens.idx();
    List<AccessFlag> modifiers = ctx.modifiersParser.parse(tokens);
    Scope scope = ModifiersParser.scope(modifiers);
    boolean isStatic = modifiers.contains(AccessFlag.ACC_STATIC);
    boolean isInterface = tokens.curr().eq(keyword(INTERFACE));
    boolean isEnum = tokens.curr().eq(keyword(ENUM));
    if (!tokens.curr().eq(keyword(CLASS)) && !isInterface && !isEnum) {
      tokens.setIdx(idx);
      return false;
    }
    if (!isStatic && !isInterface && !isEnum) {
      ctx.errors.addError(
          LOG_NAME,
          "inner (non-static nested) classes are not supported",
          new RuntimeException().getStackTrace());
    }
    Clazz outer = ctx.currClazz;
    Map<String, String> outerTypeParams = new HashMap<>(ctx.classTypeParams);
    String outerName = (outer.outerName != null ? outer.outerName + "$" : "") + outer.name;
    Clazz nested =
        parseClassDeclaration(
            tokens, outer.packageName, outer.imports, scope, isInterface, isEnum, outerName);
    ctx.currClazz = outer;
    ctx.classTypeParams.clear();
    ctx.classTypeParams.putAll(outerTypeParams);
    if (nested != null) {
      ctx.nestedClazzes.add(nested);
    }
    return true;
  }

  List<ClassMember> parseClassBody(TokenList tokens, boolean isInterface) {
    List<ClassMember> classBody = new ArrayList<>();
    while (!tokens.curr().eq(sym(RBRACE)) && !(tokens.curr() instanceof EOF)) {
      if (parseNestedClass(tokens)) {
        continue;
      }
      int start = tokens.idx();
      int errorCount = ctx.errors.errors.size();
      ClassMember member = parserClassMember(tokens, isInterface);
      if (member != null && tokens.idx() > start) {
        classBody.add(member);
        continue;
      }
      // a member that can't be parsed: report it (unless it reported an error itself) and continue
      // after it
      if (ctx.errors.errors.size() == errorCount) {
        tokens.setIdx(start);
        ctx.errors.addError(
            LOG_NAME,
            "expected a field, method or class declaration but found " + tokens.curr().display(),
            new RuntimeException().getStackTrace());
      }
      skipMember(tokens, start);
    }
    if (!classBody.isEmpty()) {
      return classBody;
    } else {
      return emptyList();
    }
  }

  private ClassMember parserClassMember(TokenList tokens, boolean isInterface) {
    Token start = tokens.curr();
    ClassMember member = parseClassMember(tokens, isInterface);
    if (member != null) {
      ctx.errors.startTokens.putIfAbsent(member, start);
    }
    return member;
  }

  private ClassMember parseClassMember(TokenList tokens, boolean isInterface) {
    if (tokens.curr().eq(keyword(STATIC))) {
      int savedIdx = tokens.idx();
      tokens.next();
      if (tokens.curr().eq(sym(LBRACE))) {
        tokens.next();
        List<Statement> body = ctx.stmtParser.parseStmtSeq(tokens);
        if (body == null) {
          body = new ArrayList<>(); // empty block
        }
        if (tokens.curr().eq(sym(RBRACE))) {
          tokens.next();
        }
        return new Method(
            "", List.of(AccessFlag.ACC_STATIC), "void", id("<clinit>"), emptyList(), body);
      }
      tokens.setIdx(savedIdx);
    }
    ClassMember member = null;
    if ((member = ctx.methodParser.parse(tokens, isInterface)) != null) {
      return member;
    } else if ((member = ctx.declParser.parse(tokens)) != null) {
      ParallelDeclaration pDecl = (ParallelDeclaration) member;
      for (Declaration decl : pDecl.declarations) {
        decl.isField = true;
      }
      if (!tokens.peekPrev().eq(sym(SEMICOLON))) {
        StatementParser.reportMissingSemicolon(ctx, tokens);
      }
      return pDecl;
    }
    return null;
  }

  // the end of the type parameters of a class, e.g. ">" (or the end of the input)
  private static boolean endsTypeParameters(Token token) {
    return token.eq(op(GREATER))
        || token.eq(op(RSHIFT))
        || token.eq(sym(LBRACE))
        || token.eq(Token.eof());
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
          } else {
            ctx.onDemandImports.add(importPath.substring(0, lastDot));
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
