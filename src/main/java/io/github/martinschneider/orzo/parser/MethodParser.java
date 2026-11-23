package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.EXTENDS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.parser.productions.Method.CONSTRUCTOR_NAME;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class MethodParser implements ProdParser<Method> {
  public ParserContext ctx;
  private static final String LOG_NAME = "parse method";

  public MethodParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  private String resolveWithWildcards(String id) {
    if (ctx.currClazz == null || ctx.currClazz.imports == null) {
      return null;
    }
    for (Import imp : ctx.currClazz.imports) {
      if (!imp.isStatic && imp.id != null && imp.id.endsWith(".*")) {
        String pkg = imp.id.substring(0, imp.id.length() - 2);
        String fqn = pkg + "." + id;
        try {
          Class.forName(fqn);
          return fqn;
        } catch (ClassNotFoundException e) {
          // not in this wildcard package
        }
      }
    }
    return null;
  }

  @Override
  public Method parse(TokenList tokens) {
    return parse(tokens, false);
  }

  public Method parse(TokenList tokens, boolean isInterface) {
    int idx = tokens.idx();
    List<AccessFlag> accFlags = new ArrayList<>();
    Type type = null;
    Identifier name;
    boolean isConstr = false;
    List<Argument> arguments = new ArrayList<>();
    List<Statement> body;
    String qualifiedName;
    accFlags.addAll(ctx.modifiersParser.parse(tokens));
    List<String> methodTypeVarsAdded = new ArrayList<>();
    if (tokens.curr().eq(op(LESS))) {
      // the type parameters, e.g. "<T, R extends Comparable<R> & Serializable>" (their type
      // arguments and further bounds are skipped)
      int depth = 0;
      boolean expectTypeVar = false;
      do {
        if (tokens.curr() instanceof EOF || tokens.curr().eq(sym(LPAREN))) {
          break;
        } else if (tokens.curr().eq(op(LESS))) {
          depth++;
          expectTypeVar = depth == 1;
          tokens.next();
        } else if (tokens.curr().eq(op(GREATER))) {
          depth--;
          tokens.next();
        } else if (tokens.curr().eq(op(RSHIFT))) {
          depth -= 2;
          tokens.next();
        } else if (tokens.curr().eq(op(RSHIFTU))) {
          depth -= 3;
          tokens.next();
        } else if (depth == 1 && tokens.curr().eq(sym(COMMA))) {
          expectTypeVar = true;
          tokens.next();
        } else if (depth == 1 && expectTypeVar && tokens.curr() instanceof Identifier) {
          expectTypeVar = false;
          String typeVarName = tokens.curr().toString();
          tokens.next();
          String bound = "java.lang.Object";
          if (tokens.curr().eq(keyword(EXTENDS))) {
            tokens.next();
            if (ctx.typeArgsParser.isQualifiedName(tokens)) {
              // a qualified bound, e.g. "T extends java.lang.reflect.Executable"
              bound = ctx.typeArgsParser.resolveQualified(parseQualifiedName(tokens));
            } else {
              String boundId = tokens.curr().toString();
              tokens.next();
              if (ctx.importMap.containsKey(boundId)) {
                bound = ctx.importMap.get(boundId);
              } else {
                try {
                  Class.forName("java.lang." + boundId);
                  bound = "java.lang." + boundId;
                } catch (ClassNotFoundException e) {
                  bound = boundId;
                }
              }
            }
          }
          if (!ctx.classTypeParams.containsKey(typeVarName)) {
            ctx.classTypeParams.put(typeVarName, bound);
            methodTypeVarsAdded.add(typeVarName);
          }
        } else {
          tokens.next();
        }
      } while (depth > 0);
    }
    if (tokens.curr() instanceof Type) {
      type = ((Type) tokens.curr());
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
    } else if (tokens.curr() instanceof Identifier
        && (ctx.typeMap.TYPES.containsKey(tokens.curr().toString())
            || ctx.currClazz.name.equals(tokens.curr().toString()))) {
      String id = tokens.curr().toString();
      type = ctx.typeMap.TYPES.getOrDefault(id, new Type(id));
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
    } else if (tokens.curr() instanceof Identifier
        && ctx.importMap.containsKey(tokens.curr().toString())
        && !ctx.typeArgsParser.isQualifiedName(tokens)) {
      String id = tokens.curr().toString();
      type = new Type(ctx.importMap.get(id));
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
    } else if (tokens.curr() instanceof Identifier
        && ctx.classTypeParams.containsKey(tokens.curr().toString())) {
      // Type variable in class declaration (e.g. S in StatementGenerator<S extends Statement>)
      String id = tokens.curr().toString();
      type = new Type(ctx.classTypeParams.get(id));
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
    } else if ((qualifiedName = parseQualifiedName(tokens)) != null) {
      // Fully-qualified return type, e.g. "java.lang.reflect.Method"
      type = new Type(qualifiedName);
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
    } else if (tokens.curr() instanceof Identifier
        && !tokens.curr().toString().isEmpty()
        && Character.isUpperCase(tokens.curr().toString().charAt(0))) {
      String id = tokens.curr().toString();
      String fqn = resolveWithWildcards(id);
      if (fqn == null) {
        try {
          Class.forName("java.lang." + id);
          fqn = "java.lang." + id;
        } catch (ClassNotFoundException e) {
          fqn =
              (ctx.currClazz != null
                      && ctx.currClazz.packageName != null
                      && !ctx.currClazz.packageName.isEmpty())
                  ? ctx.currClazz.packageName + "." + id
                  : id;
        }
      }
      type = new Type(fqn);
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
    } else {
      for (String v : methodTypeVarsAdded) ctx.classTypeParams.remove(v);
      tokens.setIdx(idx);
      return null;
    }
    // Skip generic type arguments on return type e.g. List<Statement>
    if (tokens.curr().eq(op(LESS))) {
      ctx.typeArgsParser.parse(tokens);
    }
    if (tokens.curr() instanceof Identifier) {
      name = (Identifier) tokens.curr();
      tokens.next();
    } else {
      isConstr = true;
      name = id(CONSTRUCTOR_NAME);
    }
    if (!tokens.curr().eq(sym(LPAREN))) {
      for (String v : methodTypeVarsAdded) ctx.classTypeParams.remove(v);
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    arguments = parseArgs(tokens);
    if (!tokens.curr().eq(sym(RPAREN))) {
      for (String v : methodTypeVarsAdded) ctx.classTypeParams.remove(v);
      tokens.setIdx(idx);
      return null;
    }
    body = null;
    // (an abstract method has no body, like an interface method unless default, static or private)
    boolean hasBody = !isInterface && !accFlags.contains(AccessFlag.ACC_ABSTRACT);
    if (isInterface) {
      int paramsEnd = tokens.idx();
      tokens.next();
      skipThrowsClause(tokens);
      hasBody = tokens.curr().eq(sym(LBRACE));
      tokens.setIdx(paramsEnd);
      // (interface methods are public unless private, and abstract without a body)
      if (!accFlags.contains(AccessFlag.ACC_PRIVATE) && !accFlags.contains(AccessFlag.ACC_PUBLIC)) {
        accFlags.add(AccessFlag.ACC_PUBLIC);
      }
      if (!hasBody && !accFlags.contains(AccessFlag.ACC_ABSTRACT)) {
        accFlags.add(AccessFlag.ACC_ABSTRACT);
      }
    }
    if (hasBody) {
      tokens.next();
      skipThrowsClause(tokens);
      if (!tokens.curr().eq(sym(LBRACE))) {
        for (String v : methodTypeVarsAdded) ctx.classTypeParams.remove(v);
        tokens.setIdx(idx);
        return null;
      }
      tokens.next();
      int errorCount = ctx.errors.errors.size();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (!tokens.curr().eq(sym(RBRACE))) {
        // The body ended before its "}" (at the end of the input): report it unless a statement
        // already
        // did, and skip to the "}" to keep the signature.
        if (ctx.errors.errors.size() == errorCount) {
          ctx.errors.missingExpected(
              LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
        }
        int depth = 1;
        while (depth > 0 && !(tokens.curr() instanceof EOF)) {
          tokens.next();
          if (tokens.curr().eq(sym(LBRACE))) depth++;
          else if (tokens.curr().eq(sym(RBRACE))) depth--;
        }
      }
    }
    if (body == null) {
      body = new ArrayList<>();
    }
    tokens.next();
    skipThrowsClause(tokens);
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    if (name != null && body != null) {
      if (arguments == null) {
        arguments = new ArrayList<>();
      }
      String fqn = (ctx.currClazz == null) ? null : ctx.currClazz.fqn();
      for (String v : methodTypeVarsAdded) ctx.classTypeParams.remove(v);
      if (isConstr) {
        return new Constructor(fqn, accFlags, name, arguments, body);
      } else {
        return new Method(fqn, accFlags, type.descr(), name, arguments, body);
      }
    }
    for (String v : methodTypeVarsAdded) ctx.classTypeParams.remove(v);
    return null;
  }

  List<Argument> parseArgs(TokenList tokens) {
    List<Argument> arguments = new ArrayList<>();
    while (!tokens.curr().eq(sym(RPAREN))) {
      String type = null;
      Identifier name = null;
      String qualifiedName;
      boolean varargs = false;
      if (tokens.curr() instanceof Type && !tokens.curr().eq("VOID")) {
        type = tokens.curr().toString();
      } else if (tokens.curr() instanceof Identifier
          && ctx.typeMap.TYPES.containsKey(tokens.curr().toString())) {
        type = ctx.typeMap.TYPES.get(tokens.curr().toString()).toString();
      } else if (tokens.curr() instanceof Identifier
          && ctx.importMap.containsKey(tokens.curr().toString())
          && !ctx.typeArgsParser.isQualifiedName(tokens)) {
        type = ctx.importMap.get(tokens.curr().toString());
      } else if (tokens.curr() instanceof Identifier
          && ctx.classTypeParams.containsKey(tokens.curr().toString())) {
        // Type variable (e.g. S in StatementGenerator<S extends Statement>) → erase to bound
        type = ctx.classTypeParams.get(tokens.curr().toString());
      } else if ((qualifiedName = parseQualifiedName(tokens)) != null) {
        // Fully-qualified parameter type, e.g. "java.util.List<String> items"
        type = qualifiedName;
      } else if (tokens.curr() instanceof Identifier
          && !tokens.curr().toString().isEmpty()
          && Character.isUpperCase(tokens.curr().toString().charAt(0))) {
        String id = tokens.curr().toString();
        String resolved = resolveWithWildcards(id);
        if (resolved != null) {
          type = resolved;
        } else {
          try {
            Class.forName("java.lang." + id);
            type = "java.lang." + id;
          } catch (ClassNotFoundException e) {
            type =
                (ctx.currClazz != null
                        && ctx.currClazz.packageName != null
                        && !ctx.currClazz.packageName.isEmpty())
                    ? ctx.currClazz.packageName + "." + id
                    : id;
          }
        }
      } else {
        break;
      }
      // generic type arguments e.g. List<Token> or List<List<Token>>
      String typeArgs = null;
      if (tokens.next().eq(op(LESS))) {
        typeArgs = ctx.typeArgsParser.parse(tokens);
      }
      if (tokens.curr().eq(sym(LBRAK))) {
        if (tokens.next().eq(sym(RBRAK))) {
          type = "[" + type;
        } else {
          ctx.errors.missingExpected(
              LOG_NAME, sym(RBRAK), tokens, new RuntimeException().getStackTrace());
          tokens.next();
          break;
        }
      } else if (tokens.curr().eq(sym(DOT))
          && tokens.next().eq(sym(DOT))
          && tokens.next().eq(sym(DOT))) {
        // varargs (e.g. PrintStream... outputs) — an array parameter
        type = "[" + type;
        varargs = true;
      } else {
        tokens.prev();
      }
      if (tokens.next() instanceof Identifier) {
        name = (Identifier) tokens.curr();
        // C-style array parameter, e.g. "String args[]"
        while (tokens.peekNext().eq(sym(LBRAK)) && tokens.get(tokens.idx() + 2).eq(sym(RBRAK))) {
          tokens.next();
          tokens.next();
          type = "[" + type;
        }
      }
      if (type != null && name != null) {
        Argument arg = new Argument(type, name);
        arg.varargs = varargs;
        if (typeArgs != null && !type.startsWith("[")) {
          arg.genericType = type + typeArgs;
        }
        arguments.add(arg);
      }
      if (tokens.next().eq(sym(COMMA))) {
        tokens.next();
      } else {
        break;
      }
    }
    return arguments.isEmpty() ? null : arguments;
  }

  // A fully-qualified type name, e.g. "java.lang.reflect.Method": returns the name and leaves the
  // current token at its last segment, or returns null (and leaves the index unchanged) if the
  // current token doesn't start a dotted chain ending in an uppercase segment.
  private String parseQualifiedName(TokenList tokens) {
    if (!(tokens.curr() instanceof Identifier) || !tokens.peekNext().eq(sym(DOT))) {
      return null;
    }
    int start = tokens.idx();
    StringBuilder fqn = new StringBuilder(tokens.curr().toString());
    while (tokens.peekNext().eq(sym(DOT))) {
      tokens.next();
      Token seg = tokens.next();
      if (!(seg instanceof Identifier) && !(seg instanceof Type)) {
        tokens.setIdx(start);
        return null;
      }
      fqn.append('.').append(seg instanceof Type ? ((Type) seg).name : seg.toString());
    }
    String last = tokens.curr().toString();
    if (last.isEmpty() || !Character.isUpperCase(last.charAt(0))) {
      tokens.setIdx(start);
      return null;
    }
    return ctx.typeArgsParser.resolveQualified(fqn.toString());
  }

  // Orzo does not check exceptions, so a method's "throws T1, T2, ..." clause is parsed only to
  // be discarded. "throws" lexes as a plain Identifier (there is no THROWS keyword token).
  private void skipThrowsClause(TokenList tokens) {
    if (tokens.curr() instanceof Identifier && "throws".equals(tokens.curr().val.toString())) {
      tokens.next();
      while (tokens.curr() instanceof Identifier) {
        tokens.next();
        while (tokens.curr().eq(sym(DOT))) {
          tokens.next();
          tokens.next();
        }
        if (tokens.curr().eq(sym(COMMA))) {
          tokens.next();
        } else {
          break;
        }
      }
    }
  }
}
