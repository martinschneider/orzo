package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
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
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_FINAL;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_STATIC;
import static io.github.martinschneider.orzo.parser.productions.Method.CONSTRUCTOR_NAME;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
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
    Scope scope = null;
    Type type = null;
    Identifier name;
    boolean isConstr = false;
    List<Argument> arguments = new ArrayList<>();
    List<Statement> body;
    if (tokens.curr() instanceof Scope) {
      scope = (Scope) tokens.curr();
      try {
        accFlags.add(((Scopes) scope.val).accFlag);
      } catch (NoSuchFieldError e) {
        // TODO: Temporary workaround for bootstrapping circular dependency
        // Handle case where Scopes.accFlag field doesn't exist yet (during self-compilation)
        // Use a default access flag based on the scope name
        // This should be replaced with proper dependency resolution and compilation ordering
        Scopes scopeVal = (Scopes) scope.val;
        switch (scopeVal) {
          case PUBLIC:
            accFlags.add(AccessFlag.ACC_PUBLIC);
            break;
          case PRIVATE:
            accFlags.add(AccessFlag.ACC_PRIVATE);
            break;
          case PROTECTED:
            accFlags.add(AccessFlag.ACC_PROTECTED);
            break;
          case DEFAULT:
            // No access flag needed for default
            break;
        }
      }
      tokens.next();
    }
    if (tokens.curr().eq(keyword(STATIC))) {
      accFlags.add(ACC_STATIC);
      tokens.next();
    }
    if (tokens.curr().eq(keyword(FINAL))) {
      accFlags.add(ACC_FINAL);
      tokens.next();
    }
    if (tokens.curr() instanceof Type) {
      type = ((Type) tokens.curr());
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
      // TODO: add current class name to the map instead of having a spearate condition
    } else if (tokens.curr() instanceof Identifier
        && (ctx.typeMap.TYPES.containsKey(tokens.curr().toString())
            || ctx.currClazz.name.equals(tokens.curr().toString()))) {
      String id = tokens.curr().toString();
      type = ctx.typeMap.TYPES.getOrDefault(id, new Type(id));
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
    } else if (tokens.curr() instanceof Identifier
        && ctx.importMap.containsKey(tokens.curr().toString())) {
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
      tokens.setIdx(idx);
      return null;
    }
    // Skip generic type arguments on return type e.g. List<Statement>
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
    if (tokens.curr() instanceof Identifier) {
      name = (Identifier) tokens.curr();
      tokens.next();
    } else {
      isConstr = true;
      name = id(CONSTRUCTOR_NAME);
    }
    if (!tokens.curr().eq(sym(LPAREN))) {
      ctx.errors.tokenIdx = tokens.idx();
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    arguments = parseArgs(tokens);
    if (!tokens.curr().eq(sym(RPAREN))) {
      ctx.errors.tokenIdx = tokens.idx();
      tokens.setIdx(idx);
      return null;
    }
    body = null;
    if (!isInterface) {
      tokens.next();
      if (!tokens.curr().eq(sym(LBRACE))) {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (!tokens.curr().eq(sym(RBRACE))) {
        // Recovery: body parsing stopped before the closing brace.
        // Skip forward to the matching } rather than resetting — this preserves the method
        // signature even when the body contains syntax Orzo cannot yet parse.
        ctx.errors.tokenIdx = tokens.idx();
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
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    if (name != null && body != null) {
      if (arguments == null) {
        arguments = new ArrayList<>();
      }
      String fqn = (ctx.currClazz == null) ? null : ctx.currClazz.fqn();
      if (isConstr) {
        return new Constructor(fqn, accFlags, name, arguments, body);
      } else {
        return new Method(fqn, accFlags, type.descr(), name, arguments, body);
      }
    }
    return null;
  }

  List<Argument> parseArgs(TokenList tokens) {
    List<Argument> arguments = new ArrayList<>();
    while (!tokens.curr().eq(sym(RPAREN))) {
      String type = null;
      Identifier name = null;
      if (tokens.curr() instanceof Type && !tokens.curr().eq("VOID")) {
        type = tokens.curr().toString();
      } else if (tokens.curr() instanceof Identifier
          && ctx.typeMap.TYPES.containsKey(tokens.curr().toString())) {
        type = ctx.typeMap.TYPES.get(tokens.curr().toString()).toString();
      } else if (tokens.curr() instanceof Identifier
          && ctx.importMap.containsKey(tokens.curr().toString())) {
        type = ctx.importMap.get(tokens.curr().toString());
      } else if (tokens.curr() instanceof Identifier
          && ctx.classTypeParams.containsKey(tokens.curr().toString())) {
        // Type variable (e.g. S in StatementGenerator<S extends Statement>) → erase to bound
        type = ctx.classTypeParams.get(tokens.curr().toString());
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
      if (tokens.next().eq(op(LESS))) {
        // skip generic type arguments e.g. List<Token> or List<List<Token>>
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
        // varargs (e.g. PrintStream... outputs) — treat as array parameter
        type = "[" + type;
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
