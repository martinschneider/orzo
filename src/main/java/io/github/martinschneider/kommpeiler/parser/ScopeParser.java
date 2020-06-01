package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;

import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scopes;

public class ScopeParser implements ProdParser<Scope> {
  @Override
  public Scope parse(TokenList tokens) {
    if (tokens.curr() instanceof Scope) {
      switch ((Scopes) tokens.curr().getValue()) {
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
