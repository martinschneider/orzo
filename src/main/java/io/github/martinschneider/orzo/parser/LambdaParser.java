package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.ARROW;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.LambdaExpr;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.ArrayList;
import java.util.List;

// Recognises a lambda expression ("x -> expr" or "(x, y) -> expr") and parses it into a single
// LambdaExpr leaf token wrapped in an Expression, the same way TernaryExpression represents
// "a ? b : c" - so it slots into the existing postfix-expression machinery (method call
// arguments, assignment RHS, etc.) without any changes there. Only untyped parameter lists and an
// expression body are supported (no "(Type x) -> ..." and no "{ ... }" block body) - real usages
// so far are all untyped-single-expression, and broader support can be added when needed.
public class LambdaParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse lambda expression";

  public LambdaParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Expression parse(TokenList tokens) {
    int idx = tokens.idx();
    List<String> params = tryParseParams(tokens);
    if (params == null || !tokens.curr().eq(sym(ARROW))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next(); // consume '->'
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
    // Local-first, not "(Identifier) tokens.curr()).next" inline: a cast immediately followed by
    // a field access, nested inside a parenthesized boolean condition, isn't parsed correctly by
    // ExpressionParser yet when it's not the condition's very first operand (see docs/TODO.md).
    if (tokens.curr() instanceof Identifier) {
      Identifier curr = (Identifier) tokens.curr();
      if (curr.next == null) {
        String name = curr.id();
        tokens.next();
        List<String> params = new ArrayList<>();
        params.add(name);
        return params;
      }
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
    // Not "while (true)": every path inside returns, but orzo's codegen (unlike javac) doesn't
    // recognize that and adds its own trailing bare "return;" after a literal "while (true)"
    // regardless, breaking verification for a non-void method. A boolean-variable condition isn't
    // provably infinite to either compiler, so both expect (and get) the explicit return below.
    boolean more = true;
    while (more) {
      Token t = tokens.curr();
      if (!(t instanceof Identifier)) {
        tokens.setIdx(idx);
        return null;
      }
      Identifier ident = (Identifier) t;
      if (ident.next != null) {
        tokens.setIdx(idx);
        return null;
      }
      params.add(ident.id());
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
    return null;
  }
}
