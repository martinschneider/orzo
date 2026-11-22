package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.UnaryNode;

public class PostIncrementParser implements ProdParser<IncrementStatement> {
  private ParserContext ctx;

  public PostIncrementParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  /** "a++;", "a.b[i]--;", ... */
  @Override
  public IncrementStatement parse(TokenList tokens) {
    int idx = tokens.idx();
    ExprNode target = ctx.exprParser.parseAssignmentTarget(tokens);
    if (target == null) {
      return null;
    }
    if (!tokens.curr().eq(op(POST_INCREMENT)) && !tokens.curr().eq(op(POST_DECREMENT))) {
      tokens.setIdx(idx);
      return null;
    }
    Operators op = ((Operator) tokens.curr()).opValue();
    tokens.next();
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    return new IncrementStatement(ctx.exprParser.lower(new UnaryNode(op, target)));
  }
}
