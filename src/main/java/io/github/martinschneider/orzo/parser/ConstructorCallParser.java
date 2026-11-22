package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.NEW;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.tree.CallNode;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.NewNode;

public class ConstructorCallParser implements ProdParser<ConstructorCall> {
  private ParserContext ctx;

  public ConstructorCallParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  /** "new Foo(x);", also with calls on the new object, e.g. "new Foo().bar().baz();" */
  @Override
  public ConstructorCall parse(TokenList tokens) {
    if (!tokens.curr().eq(keyword(NEW))) {
      return null;
    }
    int idx = tokens.idx();
    ExprNode tree = ctx.exprParser.parseSelectorExpression(tokens);
    if (tree instanceof NewNode || tree instanceof CallNode) {
      Token creation = ctx.exprParser.lower(tree).tokens.get(0);
      if (creation instanceof ConstructorCall) {
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
        return (ConstructorCall) creation;
      }
    }
    tokens.setIdx(idx);
    return null;
  }
}
