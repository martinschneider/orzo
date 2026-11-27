package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MonitorStatement;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * "synchronized (lock) { body }" becomes (like javac generates it)
 *
 * <pre>
 * { Object $lockN = lock; monitorenter $lockN; try { body } finally { monitorexit $lockN; } }
 * </pre>
 */
public class SynchronizedParser implements ProdParser<Statement> {
  private ParserContext ctx;
  private int count;

  public SynchronizedParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Statement parse(TokenList tokens) {
    if (!(tokens.curr() instanceof Identifier)
        || !"synchronized".equals(tokens.curr().val.toString())
        || !tokens.peekNext().eq(sym(LPAREN))) {
      return null;
    }
    int idx = tokens.idx();
    tokens.next();
    tokens.next();
    Expression lock = ctx.exprParser.parse(tokens);
    if (lock == null || !tokens.curr().eq(sym(RPAREN))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    if (!tokens.curr().eq(sym(LBRACE))) {
      tokens.setIdx(idx);
      return null;
    }
    Statement body = ctx.stmtParser.parse(tokens); // (the block)
    if (!(body instanceof Block)) {
      tokens.setIdx(idx);
      return null;
    }
    String var = "$lock" + count++;
    Declaration declaration =
        new Declaration(emptyList(), "java.lang.Object", new Identifier(var), lock);
    TryStatement tryStmt = new TryStatement(((Block) body).body, new ArrayList<>());
    tryStmt.finallyBody = new ArrayList<>(List.of(new MonitorStatement(var, false)));
    return new Block(
        new ArrayList<>(
            List.of(
                new ParallelDeclaration(new ArrayList<>(List.of(declaration))),
                new MonitorStatement(var, true),
                tryStmt)));
  }
}
