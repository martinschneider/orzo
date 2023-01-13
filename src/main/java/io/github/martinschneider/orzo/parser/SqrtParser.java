package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SQRT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Identifier;

public class SqrtParser {
  private ParserContext ctx;

  public SqrtParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  public void parse(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(sym(SQRT))) {
      Token lookAhead = tokens.next();
      if (lookAhead.isIntLit()) {
        tokens.replace(Token.doubleLit(String.valueOf(Math.sqrt(lookAhead.intVal()))));
        tokens.remove(idx);
      }
      else if (lookAhead.isLongLit()) {
            tokens.replace(Token.doubleLit(String.valueOf(Math.sqrt(lookAhead.longVal()))));
            tokens.remove(idx);
      } else if (lookAhead.isFloatLit()) {
    	  tokens.replace(Token.doubleLit(String.valueOf(Math.sqrt(lookAhead.floatVal()))));
        tokens.remove(idx);
      } else if (lookAhead.isDoubleLit()) {
    	  tokens.replace(Token.doubleLit(String.valueOf(Math.sqrt(lookAhead.doubleVal()))));
        tokens.remove(idx);
      } else if (lookAhead.isId()) {
        tokens.replace(idx, Identifier.of("Math.sqrt"));
        tokens.insert(idx + 2, sym(RPAREN));
        tokens.insert(idx + 1, sym(LPAREN));
      } else {
        tokens.replace(idx, Identifier.of("Math.sqrt"));
      }
    }
    ctx.errors.tokenIdx = tokens.idx();
    tokens.setIdx(idx);
  }
}
