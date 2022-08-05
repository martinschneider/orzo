package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.NEW;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.ArrayList;
import java.util.List;

public class ArrayInitParser implements ProdParser<ArrayInit> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse array initialiser";

  public ArrayInitParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ArrayInit parse(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(keyword(NEW))) {
      String type = null;
      List<List<Expression>> vals = new ArrayList<>();
      tokens.next();
      if (tokens.curr() instanceof Type) {
        type = ((Type) tokens.curr()).name;
        tokens.next();
      } else {
        ctx.errors.tokenIdx = tokens.idx();
        ctx.errors.addError(
            LOG_NAME, "missing type in array initialiser", new RuntimeException().getStackTrace());
        tokens.setIdx(idx);
        return null;
      }
      List<Expression> dimensions = new ArrayList<>();
      while (tokens.curr().eq(sym(LBRAK))) {
        tokens.next();
        Expression dim = ctx.exprParser.parse(tokens);
        if (dim != null) {
          dimensions.add(dim);
        } else {
          dimensions.add(null); // placeholder
        }
        if (!tokens.curr().eq(sym(RBRAK))) {
          ctx.errors.missingExpected(
              LOG_NAME, sym(RBRAK), tokens, new RuntimeException().getStackTrace());
          return null;
        }
        tokens.next();
      }
      if (tokens.curr().eq(sym(LBRACE))) {
        // TODO: support multi-dimensional initialisation
        if (dimensions.size() > 1) {
          ctx.errors.addError(
              LOG_NAME,
              "direct array initialisation not (yet) supported for multi-dimensional arrays",
              new RuntimeException().getStackTrace());
          tokens.next(sym(SEMICOLON));
        } else {
          vals.add(new ArrayList<>());
          tokens.next();
          Expression expr;
          while ((expr = ctx.exprParser.parse(tokens)) != null) {
            vals.get(0).add(expr);
            if (tokens.curr().eq(sym(COMMA))) {
              tokens.next();
            }
          }
          if (!tokens.curr().eq(sym(RBRACE))) {
            ctx.errors.missingExpected(
                LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
          }
          tokens.next();
        }
      } else {
        vals.add(new ArrayList<>());
      }
      for (int i = 0; i < vals.size(); i++) {
        if (dimensions.get(i) == null) {
          dimensions.set(i, new Expression(List.of(new IntLiteral(vals.get(i).size(), false))));
        } else {
          // this is different from the Java spec which doesn't allow specifying array
          // dimension and
          // an initializer
          // we allow it (even though it's redundant) but fail if the size and the number
          // of vals
          // doesn't match
          //
          // if dim is a literal and it doesn't match the number of values found
          if (dimensions.get(i).tokens.size() == 1
              && dimensions.get(i).tokens.get(0) instanceof IntLiteral
              && ((IntLiteral) dimensions.get(i).tokens.get(0)).intValue() != vals.get(i).size()
              && vals.get(i).size() != 0) {
            ctx.errors.addError(
                LOG_NAME,
                "array initializer size mismatch " + dimensions.get(i) + "!=" + vals.get(i).size(),
                new RuntimeException().getStackTrace());
          }
        }
      }
      return new ArrayInit(type, dimensions, vals);
    }
    return null;
  }
}
