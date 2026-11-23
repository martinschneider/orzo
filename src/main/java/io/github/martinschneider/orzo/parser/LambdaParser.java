package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.ARROW;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.LambdaExpr;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

// A lambda ("x -> expr", "(x, y) -> expr") or a method reference ("Math::min") as a single
// LambdaExpr token. Only untyped parameters and an expression body are supported.
public class LambdaParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse lambda expression";

  public LambdaParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Expression parse(TokenList tokens) {
    int idx = tokens.idx();
    if (((tokens.curr() instanceof Identifier && ((Identifier) tokens.curr()).next == null)
            || tokens.curr() instanceof Type)
        && tokens.peekNext().eq(sym(COLON))
        && tokens.get(idx + 2).eq(sym(COLON))
        && tokens.get(idx + 3) instanceof Identifier) {
      // a method reference, e.g. "Math::min", "String::length"
      String owner =
          (tokens.curr() instanceof Type)
              ? ((Type) tokens.curr()).name
              : ((Identifier) tokens.curr()).id();
      String method = ((Identifier) tokens.get(idx + 3)).id();
      tokens.setIdx(idx + 4);
      List<Token> refTokens = new ArrayList<>();
      refTokens.add(LambdaExpr.methodRef(owner, method));
      return new Expression(refTokens);
    }
    List<String> params = tryParseParams(tokens);
    if (params == null || !tokens.curr().eq(sym(ARROW))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next(); // consume '->'
    if (tokens.curr().eq(sym(LBRACE))) {
      // a block body
      tokens.next();
      List<Statement> block = ctx.stmtParser.parseStmtSeq(tokens);
      if (tokens.curr().eq(sym(RBRACE))) {
        tokens.next();
      } else {
        ctx.errors.missingExpected(
            LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
      }
      LambdaExpr lambda = new LambdaExpr(params, new Expression(new ArrayList<>()));
      lambda.block = (block != null) ? block : new ArrayList<>();
      List<Token> lambdaTokens = new ArrayList<>();
      lambdaTokens.add(lambda);
      return new Expression(lambdaTokens);
    }
    Expression body = ctx.exprParser.parse(tokens);
    if (body == null) {
      ctx.errors.addError(LOG_NAME, "missing lambda body", new RuntimeException().getStackTrace());
      tokens.setIdx(idx);
      return null;
    }
    List<Token> bodyTokens = new ArrayList<>();
    bodyTokens.add(new LambdaExpr(params, body));
    return new Expression(bodyTokens);
  }

  // Recognises either a bare identifier ("x") or a parenthesized, comma-separated, untyped
  // identifier list ("()", "(x)", "(x, y)") as a lambda parameter list. Returns null (and leaves
  // `tokens` untouched) if the upcoming tokens don't match either shape - this does NOT by itself
  // confirm a lambda (an "->" could still be missing after it, e.g. a plain parenthesized
  // expression "(x)"), so the caller still checks for "->" before committing.
  private List<String> tryParseParams(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr() instanceof Identifier && ((Identifier) tokens.curr()).next == null) {
      List<String> params = new ArrayList<>();
      params.add(((Identifier) tokens.curr()).id());
      tokens.next();
      return params;
    }
    if (!tokens.curr().eq(sym(LPAREN))) {
      return null;
    }
    tokens.next(); // consume '('
    List<String> params = new ArrayList<>();
    if (tokens.curr().eq(sym(RPAREN))) {
      tokens.next();
      return params;
    }
    while (true) {
      Token t = tokens.curr();
      if (!(t instanceof Identifier) || ((Identifier) t).next != null) {
        tokens.setIdx(idx);
        return null;
      }
      params.add(((Identifier) t).id());
      tokens.next();
      if (tokens.curr().eq(sym(COMMA))) {
        tokens.next();
        continue;
      }
      if (tokens.curr().eq(sym(RPAREN))) {
        tokens.next();
        return params;
      }
      tokens.setIdx(idx);
      return null;
    }
  }
}
