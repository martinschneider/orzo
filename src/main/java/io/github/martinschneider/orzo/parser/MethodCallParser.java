package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.tree.CallNode;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.IndexNode;

public class MethodCallParser implements ProdParser<MethodCall> {
  private ParserContext ctx;

  public MethodCallParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  /**
   * A call statement, e.g. "f(x);" or "a.b.f(x).g();" (see StatementParser for one on an array
   * element or a parenthesized expression, e.g. "a[0].f();").
   */
  @Override
  public MethodCall parse(TokenList tokens) {
    if (!(tokens.curr() instanceof Identifier)) {
      return null;
    }
    int idx = tokens.idx();
    ExprNode tree = ctx.exprParser.parseSelectorExpression(tokens);
    ExprNode call = tree;
    while (call instanceof IndexNode) {
      call = ((IndexNode) call).target; // e.g. "f()[0]"
    }
    if (call instanceof CallNode) {
      Token first = ctx.exprParser.lower(tree).tokens.get(0);
      if (first instanceof MethodCall) {
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
        return (MethodCall) first;
      }
    }
    tokens.setIdx(idx);
    return null;
  }
}
