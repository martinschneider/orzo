package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Type;

public class CastParser implements ProdParser<Type> {
  private static final String LOG_NAME = "parse cast";

  @Override
  public Type parse(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(sym(LPAREN))) {
      tokens.next();
      if (!(tokens.curr() instanceof Type)) {
        tokens.setIdx(idx);
        return null;
      }
      Type type = (Type) tokens.curr();
      tokens.next();
      if (tokens.curr().eq(sym(RPAREN))) {
        tokens.next();
        return type;
      } else {
        tokens.setIdx(idx);
        return null;
      }
    }
    return null;
  }
}
