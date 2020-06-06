package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;

public class ScopeParser implements ProdParser<Scope> {
  @Override
  public Scope parse(TokenList tokens) {
    if (tokens.curr() instanceof Scope) {
      switch ((Scopes) tokens.curr().val) {
        case PUBLIC:
          tokens.next();
          return scope(PUBLIC);
        case PRIVATE:
          tokens.next();
          return scope(PRIVATE);
        case PROTECTED:
          tokens.next();
          return scope(PROTECTED);
        default:
          tokens.next();
          return scope(DEFAULT);
      }
    }
    return null;
  }
}
