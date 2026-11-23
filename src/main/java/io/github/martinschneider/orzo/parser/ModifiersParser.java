package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.INTERFACE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Modifiers in any order, e.g. "public static final" or "static public". */
public class ModifiersParser {
  // modifiers that aren't keywords of the lexer (they are identifiers)
  private static final Map<String, AccessFlag> NAMED =
      Map.of(
          "abstract", AccessFlag.ACC_ABSTRACT,
          "synchronized", AccessFlag.ACC_SYNCHRONIZED,
          "transient", AccessFlag.ACC_TRANSIENT,
          "volatile", AccessFlag.ACC_VOLATILE,
          "strictfp", AccessFlag.ACC_STRICT);

  /** The access flags of the modifiers (none if there are none). */
  public List<AccessFlag> parse(TokenList tokens) {
    List<AccessFlag> flags = new ArrayList<>();
    while (true) {
      Token curr = tokens.curr();
      if (curr instanceof Scope) {
        flags.add(((Scopes) curr.val).accFlag);
      } else if (curr.eq(keyword(STATIC))) {
        flags.add(AccessFlag.ACC_STATIC);
      } else if (curr.eq(keyword(FINAL))) {
        flags.add(AccessFlag.ACC_FINAL);
      } else if (curr instanceof Identifier
          && NAMED.containsKey(((Identifier) curr).id())
          && isFollowedByDeclaration(tokens)) {
        flags.add(NAMED.get(((Identifier) curr).id()));
      } else {
        return flags;
      }
      tokens.next();
    }
  }

  /** The scope of the given access flags, e.g. "public" (null if there is none). */
  public static Scope scope(List<AccessFlag> flags) {
    for (Scopes scope : Scopes.values()) {
      if (scope != Scopes.DEFAULT && flags.contains(scope.accFlag)) {
        return new Scope(scope);
      }
    }
    return null;
  }

  // (e.g. not "volatile = 1;" with a variable named volatile)
  private static boolean isFollowedByDeclaration(TokenList tokens) {
    Token next = tokens.peekNext();
    return next instanceof Identifier
        || next instanceof io.github.martinschneider.orzo.lexer.tokens.Type
        || next instanceof Scope
        || next.eq(keyword(STATIC))
        || next.eq(keyword(FINAL))
        || next.eq(keyword(CLASS))
        || next.eq(keyword(INTERFACE))
        || next.eq(Token.op(io.github.martinschneider.orzo.lexer.tokens.Operators.LESS));
  }
}
