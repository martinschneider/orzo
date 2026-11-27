package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.ENUM;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.INTERFACE;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rewrites the record declarations of a token list into classes (before they are parsed), e.g.
 * "record Point(int x, int y) { ... }" into
 *
 * <pre>
 * final class Point extends java.lang.Record {
 *   private final int x; private final int y;
 *   Point(int x, int y) { (the compact constructor's body, if any) this.x = x; this.y = y; }
 *   public int x() { return this.x; } public int y() { return this.y; }
 *   public String toString() { return "Point[x=" + x + ", y=" + y + "]"; }
 *   public boolean equals(Object o) { ... } public int hashCode() { ... }
 *   ...
 * }
 * </pre>
 *
 * (without the members the record declares itself). A member record is static.
 */
public class RecordRewriter {
  private static final Map<String, String> WRAPPERS =
      Map.of(
          "int", "Integer",
          "long", "Long",
          "short", "Short",
          "byte", "Byte",
          "char", "Character",
          "boolean", "Boolean",
          "float", "Float",
          "double", "Double");

  // a component of a record header, e.g. "int x"
  private static class Component {
    List<Token> type = new ArrayList<>();
    String name;

    // the type as a field's, e.g. "int[]" for the varargs component "int... xs" (new tokens: the
    // parser changes some, e.g. the dimensions of a Type)
    List<Token> fieldType(Token at) {
      StringBuilder text = new StringBuilder();
      for (int i = 0; i < type.size(); i++) {
        if (type.get(i).eq(sym(DOT))
            && i + 2 < type.size()
            && type.get(i + 1).eq(sym(DOT))
            && type.get(i + 2).eq(sym(DOT))) {
          text.append("[]");
          i += 2;
        } else {
          text.append(' ').append(type.get(i).text);
        }
      }
      return lex(text.toString(), at);
    }

    // the primitive type (null for a reference type)
    String primitive() {
      return (type.size() == 1 && type.get(0) instanceof Type && WRAPPERS.containsKey(name(type)))
          ? name(type)
          : null;
    }

    private static String name(List<Token> type) {
      return ((Type) type.get(0)).name;
    }
  }

  public static void rewrite(TokenList tokens) {
    List<Token> list = tokens.list();
    // the kinds of the enclosing braces: true for a class body
    List<Boolean> braces = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      Token token = list.get(i);
      if (token.eq(sym(LBRACE))) {
        braces.add(opensClassBody(list, i));
      } else if (token.eq(sym(RBRACE)) && !braces.isEmpty()) {
        braces.remove(braces.size() - 1);
      } else if (isRecord(list, i)) {
        boolean member = !braces.isEmpty() && braces.get(braces.size() - 1);
        rewrite(list, i, member);
      }
    }
  }

  // "record Name(" or "record Name<"
  private static boolean isRecord(List<Token> list, int i) {
    return list.get(i) instanceof Identifier
        && "record".equals(list.get(i).val.toString())
        && ((Identifier) list.get(i)).next == null
        && i + 2 < list.size()
        && list.get(i + 1) instanceof Identifier
        && (list.get(i + 2).eq(sym(LPAREN)) || list.get(i + 2).eq(op(LESS)));
  }

  // whether the "{" at the given index opens the body of a class, interface, enum or record
  private static boolean opensClassBody(List<Token> list, int brace) {
    for (int i = brace - 1; i >= 0; i--) {
      Token token = list.get(i);
      if (token.eq(sym(SEMICOLON)) || token.eq(sym(LBRACE)) || token.eq(sym(RBRACE))) {
        return false;
      } else if (token.eq(keyword(CLASS))
          || token.eq(keyword(INTERFACE))
          || token.eq(keyword(ENUM))) {
        return true;
      } else if (token.eq(sym(RPAREN)) || token.eq(op(GREATER))) {
        // e.g. "new Foo() {" (an anonymous class)
        continue;
      }
    }
    return false;
  }

  private static void rewrite(List<Token> list, int start, boolean member) {
    Token at = list.get(start);
    String name = list.get(start + 1).val.toString();
    int i = start + 2;
    List<Token> typeParams = new ArrayList<>();
    if (list.get(i).eq(op(LESS))) {
      int end = typeParametersEnd(list, i);
      typeParams.addAll(list.subList(i, end));
      i = end;
    }
    // the header, e.g. "(int x, List<String> y)"
    List<Component> components = new ArrayList<>();
    int headerStart = i + 1;
    i = headerStart;
    Component component = new Component();
    for (int depth = 0; !list.get(i).eq(sym(RPAREN)) || depth > 0; i++) {
      Token token = list.get(i);
      if (token instanceof EOF) {
        return;
      } else if (token.eq(op(LESS))) {
        depth++;
      } else if (token.eq(op(GREATER))) {
        depth--;
      } else if (token.eq(op(RSHIFT))) {
        depth -= 2;
      } else if (token.eq(op(RSHIFTU))) {
        depth -= 3;
      }
      if (depth == 0 && token.eq(sym(COMMA))) {
        components.add(component);
        component = new Component();
      } else if (!(token instanceof Scope) && !token.eq(keyword(FINAL))) {
        component.type.add(token);
      }
    }
    if (!component.type.isEmpty()) {
      components.add(component);
    }
    for (Component c : components) {
      Token last = c.type.remove(c.type.size() - 1);
      c.name = last.val.toString();
    }
    List<Token> header = new ArrayList<>(list.subList(headerStart, i));
    i++; // ")"
    // "implements A, B" (up to the body)
    int bodyStart = i;
    while (!list.get(bodyStart).eq(sym(LBRACE)) && !(list.get(bodyStart) instanceof EOF)) {
      bodyStart++;
    }
    List<Token> supertypes = new ArrayList<>(list.subList(i, bodyStart));
    int bodyEnd = matchingBrace(list, bodyStart);
    if (bodyEnd < 0) {
      return;
    }
    List<Token> body = new ArrayList<>(list.subList(bodyStart + 1, bodyEnd));
    List<Token> compact = removeCompactConstructor(body, name);

    List<Token> out = new ArrayList<>();
    out.addAll(lex((member ? "static " : "") + "final class " + name, at));
    out.addAll(typeParams);
    out.addAll(lex("extends java.lang.Record", at));
    out.addAll(supertypes);
    out.add(sym(LBRACE));
    for (Component c : components) {
      out.addAll(lex("private final", at));
      out.addAll(c.fieldType(at));
      out.addAll(lex(c.name + ";", at));
    }
    if (!declaresMethod(body, name, components.size())) { // (the canonical constructor)
      out.addAll(lex(name + "(", at));
      out.addAll(copy(header, at));
      out.addAll(lex(") {", at));
      if (compact != null) {
        out.addAll(compact);
      }
      for (Component c : components) {
        out.addAll(lex("this." + c.name + " = " + c.name + ";", at));
      }
      out.add(sym(RBRACE));
    }
    for (Component c : components) {
      if (!declaresMethod(body, c.name, 0)) {
        out.addAll(lex("public", at));
        out.addAll(c.fieldType(at));
        out.addAll(lex(c.name + "() { return this." + c.name + "; }", at));
      }
    }
    if (!declaresMethod(body, "toString", 0)) {
      StringBuilder str = new StringBuilder("public String toString() { return \"" + name + "[");
      for (int j = 0; j < components.size(); j++) {
        String c = components.get(j).name;
        str.append((j > 0) ? " + \", " : "").append(c).append("=\" + ").append(c);
      }
      str.append(components.isEmpty() ? "]\"; }" : " + \"]\"; }");
      out.addAll(lex(str.toString(), at));
    }
    if (!declaresMethod(body, "equals", 1)) {
      StringBuilder str =
          new StringBuilder(
              "public boolean equals(Object $o) { if (this == $o) return true; "
                  + "if (!($o instanceof "
                  + name
                  + ")) return false; "
                  + name
                  + " $other = ("
                  + name
                  + ") $o; return true");
      for (Component c : components) {
        String primitive = c.primitive();
        if (primitive == null) {
          str.append(" && java.util.Objects.equals(" + c.name + ", $other." + c.name + ")");
        } else if (primitive.equals("float") || primitive.equals("double")) {
          str.append(" && " + WRAPPERS.get(primitive) + ".compare(" + c.name);
          str.append(", $other." + c.name + ") == 0");
        } else {
          str.append(" && " + c.name + " == $other." + c.name);
        }
      }
      out.addAll(lex(str.append("; }").toString(), at));
    }
    if (!declaresMethod(body, "hashCode", 0)) {
      // (like java.lang.runtime.ObjectMethods: 31 * h + the component's hash code)
      StringBuilder str = new StringBuilder("public int hashCode() { int $h = 0;");
      for (Component c : components) {
        String primitive = c.primitive();
        String hash =
            (primitive != null)
                ? WRAPPERS.get(primitive) + ".hashCode(" + c.name + ")"
                : "java.util.Objects.hashCode(" + c.name + ")";
        str.append(" $h = 31 * $h + " + hash + ";");
      }
      out.addAll(lex(str.append(" return $h; }").toString(), at));
    }
    out.addAll(body);
    out.add(sym(RBRACE));
    List<Token> declaration = list.subList(start, bodyEnd + 1);
    declaration.clear();
    declaration.addAll(out);
  }

  private static int typeParametersEnd(List<Token> list, int start) {
    int depth = 0;
    int i = start;
    do {
      Token token = list.get(i);
      if (token.eq(op(LESS))) {
        depth++;
      } else if (token.eq(op(GREATER))) {
        depth--;
      } else if (token.eq(op(RSHIFT))) {
        depth -= 2;
      } else if (token instanceof EOF) {
        return i;
      }
      i++;
    } while (depth > 0);
    return i;
  }

  private static int matchingBrace(List<Token> list, int open) {
    int depth = 0;
    for (int i = open; i < list.size(); i++) {
      if (list.get(i).eq(sym(LBRACE))) {
        depth++;
      } else if (list.get(i).eq(sym(RBRACE)) && --depth == 0) {
        return i;
      } else if (list.get(i) instanceof EOF) {
        return -1;
      }
    }
    return -1;
  }

  // The body (without its braces) of the compact constructor "Name { ... }" (also with
  // modifiers), removed from the record's body, or null if there is none
  private static List<Token> removeCompactConstructor(List<Token> body, String name) {
    int depth = 0;
    for (int i = 0; i < body.size(); i++) {
      Token token = body.get(i);
      if (token.eq(sym(LBRACE))) {
        depth++;
      } else if (token.eq(sym(RBRACE))) {
        depth--;
      } else if (depth == 0
          && token instanceof Identifier
          && name.equals(token.val.toString())
          && i + 1 < body.size()
          && body.get(i + 1).eq(sym(LBRACE))) {
        int start = i;
        while (start > 0 && body.get(start - 1) instanceof Scope) {
          start--; // e.g. "public"
        }
        int end = matchingBrace(body, i + 1);
        if (end < 0) {
          return null;
        }
        List<Token> constructor = new ArrayList<>(body.subList(i + 2, end));
        body.subList(start, end + 1).clear();
        return constructor;
      }
    }
    return null;
  }

  // Whether the body declares a method (or constructor) "name(...)" with the given number of
  // parameters (at its top level, followed by its body or a throws clause)
  private static boolean declaresMethod(List<Token> body, String name, int params) {
    int depth = 0;
    for (int i = 0; i < body.size(); i++) {
      Token token = body.get(i);
      if (token.eq(sym(LBRACE))) {
        depth++;
      } else if (token.eq(sym(RBRACE))) {
        depth--;
      } else if (depth == 0
          && token instanceof Identifier
          && name.equals(token.val.toString())
          && i + 1 < body.size()
          && body.get(i + 1).eq(sym(LPAREN))
          && (i == 0 || !body.get(i - 1).eq(sym(DOT)))) {
        int count = 0;
        int j = i + 2;
        boolean empty = body.get(j).eq(sym(RPAREN));
        for (int parens = 0; j < body.size() && (parens > 0 || !body.get(j).eq(sym(RPAREN))); j++) {
          if (body.get(j).eq(sym(LPAREN)) || body.get(j).eq(op(LESS))) {
            parens++;
          } else if (body.get(j).eq(sym(RPAREN)) || body.get(j).eq(op(GREATER))) {
            parens--;
          } else if (parens == 0 && body.get(j).eq(sym(COMMA))) {
            count++;
          }
        }
        int args = empty ? 0 : count + 1;
        Token after = (j + 1 < body.size()) ? body.get(j + 1) : null;
        boolean declaration =
            after != null
                && (after.eq(sym(LBRACE))
                    || (after instanceof Identifier && "throws".equals(after.val.toString())));
        if (declaration && args == params) {
          return true;
        }
      }
    }
    return false;
  }

  // new tokens like the given ones
  private static List<Token> copy(List<Token> tokens, Token at) {
    StringBuilder text = new StringBuilder();
    for (Token token : tokens) {
      text.append(' ').append(token.text);
    }
    return lex(text.toString(), at);
  }

  private static List<Token> lex(String code, Token at) {
    List<Token> tokens = new ArrayList<>(new Lexer().getTokens(code).list());
    tokens.removeIf(token -> token instanceof EOF);
    for (Token token : tokens) {
      token.line = at.line;
      token.col = at.col;
    }
    return tokens;
  }
}
