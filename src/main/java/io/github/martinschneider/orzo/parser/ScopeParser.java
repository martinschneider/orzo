package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Scope;

public class ScopeParser implements ProdParser<Scope> {
  @Override
  public Scope parse(TokenList tokens) {
    if (tokens.curr().isScope()) {
      return tokens.curr().scopeVal();
    }
    return null;
  }
}
