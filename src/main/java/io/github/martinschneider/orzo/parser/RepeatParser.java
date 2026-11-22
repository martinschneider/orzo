package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.REPEAT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.*;
import static io.github.martinschneider.orzo.lexer.tokens.Token.*;

import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.parser.productions.*;
import io.github.martinschneider.orzo.parser.tree.BinaryNode;
import io.github.martinschneider.orzo.parser.tree.NameNode;
import java.util.ArrayList;
import java.util.List;

public class RepeatParser implements ProdParser<LoopStatement> {
  private static final String LOG_NAME = "parse repeat";
  ParserContext ctx;

  public RepeatParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public LoopStatement parse(TokenList tokens) {
    Expression count;
    List<Statement> body;
    if (tokens.curr() == null) {
      return null;
    }
    if (tokens.curr().eq(keyword(REPEAT))) {
      tokens.next();
      count = ctx.exprParser.parse(tokens);
      if (!tokens.curr().eq(sym(LBRACE))) {
        ctx.errors.missingExpected(
            LOG_NAME, sym(LBRACE), tokens, new RuntimeException().getStackTrace());
        tokens.next(sym(RBRACE));
        return null;
      }
      tokens.next();
      body = ctx.stmtParser.parseStmtSeq(tokens);
      if (body == null) {
        body = new ArrayList<>(); // an empty body
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        ctx.errors.missingExpected(
            LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
        tokens.next(sym(RBRACE));
        return null;
      } else {
        tokens.next();
      }
      if (count != null && count.tokens.isEmpty()) {
        return null; // e.g. "repeat () {}" (reported by the expression parser)
      }
      if (count == null) {
        // infinite loop
        return new WhileStatement(new Expression(List.of(bool("true"))), body);
      }
      // "for (int $repeatN = 0; $repeatN < count; $repeatN++)": a name that can't clash with the
      // program's variables (it used to be "i", overwriting a variable "i" of the program)
      String counter = "$repeat" + ctx.repeatCount++;
      Lexer lexer = new Lexer();
      Expression condition =
          ctx.exprParser.lower(
              new BinaryNode(
                  Operators.LESS, new NameNode(counter, false), ExpressionParser.tree(count)));
      return new ForStatement(
          ctx.stmtParser.parseWithoutSemicolon(lexer.getTokens("int " + counter + "=0")),
          condition,
          ctx.stmtParser.parseWithoutSemicolon(lexer.getTokens(counter + "++")),
          body);
    } else {
      return null;
    }
  }
}
