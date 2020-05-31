package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.NEW;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.ArrayInitialiser;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Type;
import java.util.ArrayList;
import java.util.List;

public class ArrayInitParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse array initialiser";

  public ArrayInitParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Expression parse(TokenList tokens) {
    if (tokens.curr().eq(keyword(NEW))) {
      int dim = -1;
      String type = null;
      List<Expression> values = new ArrayList<>();
      tokens.next();
      if (tokens.curr() instanceof Type) {
        type = ((Type) tokens.curr()).getName();
        tokens.next();
      } else {
        ctx.errors.addError(LOG_NAME, "missing type in array initialiser");
      }
      if (!tokens.curr().eq(sym(LBRAK))) {
        ctx.errors.missingExpected(LOG_NAME, sym(LBRAK), tokens);
        return null;
      }
      tokens.next();
      if (tokens.curr() instanceof IntNum) {
        long size = ((IntNum) tokens.curr()).intValue();
        if (size > Integer.MAX_VALUE || size < 0) {
          ctx.errors.addError(LOG_NAME, "invalid array size: " + size, tokens);
          tokens.next();
        } else {
          dim = (int) size;
          tokens.next();
        }
      }
      if (!tokens.curr().eq(sym(RBRAK))) {
        ctx.errors.missingExpected(LOG_NAME, sym(RBRAK), tokens);
        return null;
      }
      tokens.next();
      if (tokens.curr().eq(sym(LBRACE))) {
        tokens.next();
        Expression expr;
        while ((expr = ctx.exprParser.parse(tokens)) != null) {
          values.add(expr);
          if (tokens.curr().eq(sym(COMMA))) {
            tokens.next();
          }
        }
        if (!tokens.curr().eq(sym(RBRACE))) {
          ctx.errors.missingExpected(LOG_NAME, sym(RBRACE), tokens);
        }
        tokens.next();
      }
      if (dim == -1) {
        dim = values.size();
      } else {
        // this is different from the Java spec which doesn't allow specifying array dimension and
        // an initializer
        // we allow it (even though it's redundant) but fail if the size and the number of values
        // doen't match
        if (dim != values.size()) {
          ctx.errors.addError(
              LOG_NAME, "array initializer size mismatch " + dim + "!=" + values.size());
        }
      }
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
      return new ArrayInitialiser(type, dim, values);
    }
    return null;
  }
}
