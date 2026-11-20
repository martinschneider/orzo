package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.EXTENDS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.QUESTION;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Import;
import java.util.Map;

/**
 * Parses generic type arguments, e.g. {@code <String, List<Statement>>}, into a string of
 * fully-qualified type names, e.g. {@code <java.lang.String,java.util.List<a.b.Statement>>}.
 */
public class TypeArgsParser {
  private static final String OBJECT = "java.lang.Object";
  private static final Map<String, String> PRIMITIVE_ARRAY_CODES =
      Map.of(
          "boolean", "[Z", "byte", "[B", "char", "[C", "short", "[S", "int", "[I", "long", "[J",
          "float", "[F", "double", "[D");
  private ParserContext ctx;

  public TypeArgsParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Expects the current token to be "<". Consumes everything up to and including the matching
   * closing bracket. Returns the resolved type arguments, or null if they contain anything this
   * parser doesn't model - the tokens are consumed either way.
   */
  public String parse(TokenList tokens) {
    StringBuilder str = new StringBuilder("<");
    boolean supported = true;
    int depth = 1;
    while (depth > 0) {
      Token curr = tokens.next();
      if (curr.eq(op(LESS))) {
        depth++;
        str.append('<');
      } else if (curr.eq(op(GREATER))) {
        depth--;
        str.append('>');
      } else if (curr.eq(op(RSHIFT))) {
        depth = Math.max(0, depth - 2);
        str.append(">>");
      } else if (curr.eq(op(RSHIFTU))) {
        depth = Math.max(0, depth - 3);
        str.append(">>>");
      } else if (curr.eq(sym(COMMA))) {
        str.append(',');
      } else if (curr.eq(sym(QUESTION))) {
        // "?" or "? extends Bound" erases to the bound; "? super Bound" to Object
        Token next = tokens.peekNext();
        if (next.eq(keyword(EXTENDS))) {
          tokens.next();
        } else if (next instanceof Identifier && "super".equals(next.val.toString())) {
          tokens.next();
          tokens.next();
          str.append(OBJECT);
        } else {
          str.append(OBJECT);
        }
      } else if (curr instanceof Type || curr instanceof Identifier) {
        String name = (curr instanceof Type) ? ((Type) curr).name : curr.val.toString();
        if (tokens.peekNext().eq(sym(DOT))) {
          // already fully qualified, e.g. java.util.List
          StringBuilder fqn = new StringBuilder(name);
          while (tokens.peekNext().eq(sym(DOT))) {
            tokens.next();
            Token seg = tokens.next();
            fqn.append('.').append(seg instanceof Type ? ((Type) seg).name : seg.val.toString());
          }
          name = fqn.toString();
        } else if (!PRIMITIVE_ARRAY_CODES.containsKey(name)) {
          name = resolve(name);
        }
        // array type argument, e.g. Map<String, byte[]>: use the Class.getName() form ("[B")
        while (tokens.peekNext().eq(sym(LBRAK)) && tokens.get(tokens.idx() + 2).eq(sym(RBRAK))) {
          tokens.next();
          tokens.next();
          name =
              name.startsWith("[")
                  ? "[" + name
                  : PRIMITIVE_ARRAY_CODES.getOrDefault(name, "[L" + name + ";");
        }
        str.append(name);
      } else {
        supported = false;
      }
    }
    tokens.next();
    return supported ? str.toString() : null;
  }

  /** Resolves a simple type name (as written in the source) to a fully-qualified name. */
  public String resolve(String id) {
    if ("String".equals(id)) {
      return "java.lang.String";
    }
    if (ctx.typeMap.TYPES.containsKey(id)) {
      return ctx.typeMap.TYPES.get(id).name.replace('/', '.');
    }
    if (ctx.importMap.containsKey(id)) {
      return ctx.importMap.get(id);
    }
    if (ctx.classTypeParams.containsKey(id)) {
      return ctx.classTypeParams.get(id);
    }
    if (ctx.currClazz != null && ctx.currClazz.imports != null) {
      for (Import imp : ctx.currClazz.imports) {
        if (!imp.isStatic && imp.id != null && imp.id.endsWith(".*")) {
          String fqn = imp.id.substring(0, imp.id.length() - 2) + "." + id;
          try {
            Class.forName(fqn);
            return fqn;
          } catch (ClassNotFoundException e) {
            // not in this wildcard package
          }
        }
      }
    }
    try {
      Class.forName("java.lang." + id);
      return "java.lang." + id;
    } catch (ClassNotFoundException e) {
      // not in java.lang
    }
    if (ctx.currClazz != null
        && ctx.currClazz.packageName != null
        && !ctx.currClazz.packageName.isEmpty()) {
      return ctx.currClazz.packageName + "." + id;
    }
    return id;
  }
}
