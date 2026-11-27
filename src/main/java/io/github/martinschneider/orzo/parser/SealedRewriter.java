package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.INTERFACE;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.List;

/**
 * Removes the modifiers "sealed" and "non-sealed" and the "permits" clauses of class headers from a
 * token list (before it is parsed): they only restrict which classes may extend a class (the
 * PermittedSubclasses attribute isn't written).
 */
public class SealedRewriter {
  public static void rewrite(TokenList tokens) {
    List<Token> list = tokens.list();
    for (int i = 0; i < list.size(); i++) {
      if (isName(list.get(i), "non")
          && i + 2 < list.size()
          && list.get(i + 1).eq(op(MINUS))
          && isName(list.get(i + 2), "sealed")
          && isModifierOrType(list.get(i + 3))) {
        list.subList(i, i + 3).clear();
        i--;
      } else if (isName(list.get(i), "sealed") && isModifierOrType(list.get(i + 1))) {
        list.remove(i);
        i--;
      } else if (isName(list.get(i), "permits")
          && list.get(i + 1) instanceof Identifier
          && inClassHeader(list, i)) {
        int end = i;
        while (!list.get(end).eq(sym(LBRACE)) && !(list.get(end) instanceof EOF)) {
          end++;
        }
        list.subList(i, end).clear();
        i--;
      }
    }
  }

  private static boolean isName(Token token, String name) {
    return token instanceof Identifier
        && ((Identifier) token).next == null
        && name.equals(token.val.toString());
  }

  // a modifier, "class", "interface" or "record" (what may follow "sealed")
  private static boolean isModifierOrType(Token token) {
    return token instanceof Scope
        || token instanceof Keyword
        || isName(token, "abstract")
        || isName(token, "strictfp")
        || isName(token, "record")
        || isName(token, "sealed")
        || isName(token, "non");
  }

  // whether the token at the given index is part of the header of a class or interface
  private static boolean inClassHeader(List<Token> list, int idx) {
    for (int i = idx - 1; i >= 0; i--) {
      Token token = list.get(i);
      if (token.eq(sym(SEMICOLON)) || token.eq(sym(LBRACE)) || token.eq(sym(RBRACE))) {
        return false;
      } else if (token.eq(keyword(CLASS)) || token.eq(keyword(INTERFACE))) {
        return true;
      }
    }
    return false;
  }
}
