package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.UnaryNode;

public class PreIncrementParser implements ProdParser<IncrementStatement> {
  private ParserContext ctx;

  public PreIncrementParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  /** "++a;", "--a.b[i];", ... */
  @Override
  public IncrementStatement parse(TokenList tokens) {
    if (!tokens.curr().eq(op(PRE_INCREMENT)) && !tokens.curr().eq(op(PRE_DECREMENT))) {
      return null;
    }
    int idx = tokens.idx();
    Operators op = ((Operator) tokens.curr()).opValue();
    tokens.next();
    ExprNode target = ctx.exprParser.parseAssignmentTarget(tokens);
    if (target == null) {
      tokens.setIdx(idx);
      return null;
    }
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    return new IncrementStatement(ctx.exprParser.lower(new UnaryNode(op, target)));
  }
}
