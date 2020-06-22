package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SQRT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Token;

public class SqrtParser {
  private static final String LOG_NAME = "parse sqrt";

  public void parse(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(sym(SQRT))) {
      Token lookAhead = tokens.next();
      if (lookAhead instanceof IntLiteral) {
        tokens.replace(new FPLiteral(Math.sqrt(((IntLiteral) lookAhead).intValue()), false));
        tokens.remove(idx);
      } else if (lookAhead instanceof FPLiteral) {
        tokens.replace(new FPLiteral(Math.sqrt(((FPLiteral) lookAhead).doubleVal()), false));
        tokens.remove(idx);
      } else if (lookAhead instanceof Identifier) {
        tokens.replace(idx, new Identifier("Math.sqrt"));
        tokens.insert(idx + 2, sym(RPAREN));
        tokens.insert(idx + 1, sym(LPAREN));
      } else {
        tokens.replace(idx, new Identifier("Math.sqrt"));
      }
    }
    tokens.setIdx(idx);
  }
}
