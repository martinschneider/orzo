package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
import io.github.martinschneider.orzo.parser.productions.EmptyStatement;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.FieldAssignment;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.LocalClassDeclaration;
import io.github.martinschneider.orzo.parser.productions.LoopStatement;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.util.ArrayList;
import java.util.List;

public class StatementParser implements ProdParser<Statement> {
  private static final String LOG_NAME = "parse statement";
  private ParserContext ctx;

  public StatementParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  /**
   * The statements up to the closing '}' of a block (or the end of the input). A statement that
   * can't be parsed is reported (unless it reported an error itself) and skipped, so that the
   * following statements are still checked.
   */
  public List<Statement> parseStmtSeq(TokenList tokens) {
    List<Statement> stmtSequence = new ArrayList<>();
    while (!tokens.curr().eq(sym(RBRACE)) && !(tokens.curr() instanceof EOF)) {
      int start = tokens.idx();
      int errorCount = ctx.errors.errors.size();
      Statement stmt = parse(tokens);
      if (stmt != null && tokens.idx() > start) {
        stmtSequence.add(stmt);
        continue;
      }
      if (ctx.errors.errors.size() == errorCount) {
        tokens.setIdx(start);
        ctx.errors.addError(
            LOG_NAME,
            "expected a statement but found " + tokens.curr().display(),
            new RuntimeException().getStackTrace());
      }
      skipStatement(tokens, start);
    }
    return (stmtSequence.size() == 0) ? null : stmtSequence;
  }

  // Error recovery: continues after the next ';' (or before the '}' that closes the block)
  private void skipStatement(TokenList tokens, int start) {
    if (tokens.idx() < start) {
      tokens.setIdx(start);
    }
    int depth = 0;
    boolean moved = tokens.idx() > start;
    while (!(tokens.curr() instanceof EOF)) {
      if (tokens.curr().eq(sym(LBRACE))) {
        depth++;
      } else if (tokens.curr().eq(sym(RBRACE))) {
        if (depth == 0) {
          if (!moved) {
            tokens.next(); // (no progress otherwise)
          }
          return;
        }
        depth--;
      } else if (depth == 0 && tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
        return;
      }
      tokens.next();
      moved = true;
    }
  }

  // A call chain none of the parsers above handle, e.g. "arr[i].append(x);"
  private Statement parseExpressionStatement(TokenList tokens) {
    if (!(tokens.curr() instanceof Identifier) && !tokens.curr().eq(sym(LPAREN))) {
      return null; // (a parenthesized start, e.g. "((Foo) x).count = 1;")
    }
    int idx = tokens.idx();
    Expression expr = ctx.exprParser.parse(tokens);
    // "<call chain>.field = value;", e.g. "map.get(k).count = 1;" (parsed as an embedded
    // assignment: [target, value..., ASSIGN])
    if (expr != null
        && expr.tokens.size() >= 3
        && expr.tokens.get(expr.tokens.size() - 1).eq(op(ASSIGN))
        && (expr.tokens.get(0) instanceof MethodCall
            || expr.tokens.get(0) instanceof CastMethodCall)
        && tokens.curr().eq(sym(SEMICOLON))) {
      Identifier last = (Identifier) expr.tokens.get(0);
      while (last.next != null) {
        last = last.next;
      }
      if (!(last instanceof MethodCall) && last != expr.tokens.get(0)) {
        tokens.next();
        Expression value =
            new Expression(new ArrayList<>(expr.tokens.subList(1, expr.tokens.size() - 1)));
        return new FieldAssignment((Identifier) expr.tokens.get(0), value);
      }
    }
    if (expr != null
        && expr.tokens.size() == 1
        && expr.tokens.get(0) instanceof Identifier
        && tokens.curr().eq(sym(SEMICOLON))) {
      Identifier last = (Identifier) expr.tokens.get(0);
      while (last.next != null) {
        last = last.next;
      }
      if (last instanceof MethodCall) {
        tokens.next();
        return new ExpressionStatement(expr);
      }
    }
    tokens.setIdx(idx);
    return null;
  }

  /** A statement; one that doesn't end with a block (e.g. "x = 1;") must end with a ';'. */
  @Override
  public Statement parse(TokenList tokens) {
    Statement stmt = parseWithoutSemicolon(tokens);
    if (stmt != null && !isCompound(stmt) && !tokens.peekPrev().eq(sym(SEMICOLON))) {
      reportMissingSemicolon(ctx, tokens);
    }
    return stmt;
  }

  /** A statement not ended by a ';', e.g. the update of a for loop ("i++" in "for (...; i++)"). */
  public Statement parseWithoutSemicolon(TokenList tokens) {
    Token start = tokens.curr();
    Statement stmt = parseStatement(tokens);
    if (stmt != null) {
      // (for the position of errors when generating its code)
      ctx.errors.startTokens.putIfAbsent(stmt, start);
    }
    return stmt;
  }

  // statements ending with a block (or another statement) rather than a ';'
  private static boolean isCompound(Statement stmt) {
    return stmt instanceof IfStatement
        || stmt instanceof WhileStatement
        || stmt instanceof ForStatement
        || stmt instanceof ForEachStatement
        || stmt instanceof TryStatement
        || stmt instanceof SwitchStatement
        || stmt instanceof Block
        || stmt instanceof LocalClassDeclaration
        || stmt instanceof EmptyStatement;
  }

  // "expected ';'" right after the previous token (where the ';' is missing)
  static void reportMissingSemicolon(ParserContext ctx, TokenList tokens) {
    Token last = tokens.peekPrev();
    Token at = new Token(null);
    at.line = last.line;
    at.col = last.col + ((last.text != null) ? last.text.length() : 1);
    ctx.errors.addError(at, LOG_NAME, "expected ';'", new RuntimeException().getStackTrace());
  }

  private Statement parseStatement(TokenList tokens) {
    Statement stmt;
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next(); // an empty statement, e.g. after a block: "{ ... };"
      return new EmptyStatement();
    } else if (tokens.curr().eq(sym(LBRACE))) {
      // a block, e.g. "{ int x = 1; ... }"
      int idx = tokens.idx();
      tokens.next();
      List<Statement> body = parseStmtSeq(tokens);
      if (tokens.curr().eq(sym(RBRACE))) {
        tokens.next();
        return new Block((body != null) ? body : new ArrayList<>());
      }
      tokens.setIdx(idx);
      return null;
    }
    // a labeled loop, e.g. "outer: for (...) {...}"
    if (tokens.curr() instanceof Identifier && tokens.peekNext().eq(sym(COLON))) {
      int idx = tokens.idx();
      String label = tokens.curr().val.toString();
      tokens.next();
      tokens.next();
      if (tokens.curr().eq(sym(LBRACE))) {
        // a labeled block, e.g. "outer: { ... break outer; ... }"
        tokens.next();
        List<Statement> body = parseStmtSeq(tokens);
        if (tokens.curr().eq(sym(RBRACE))) {
          tokens.next();
          Block block = new Block((body != null) ? body : new ArrayList<>());
          block.label = label;
          return block;
        }
        tokens.setIdx(idx);
        return null;
      }
      stmt = parse(tokens);
      if (stmt instanceof LoopStatement) {
        ((LoopStatement) stmt).label = label;
        return stmt;
      } else if (stmt instanceof ForEachStatement) {
        ((ForEachStatement) stmt).label = label;
        return stmt;
      }
      tokens.setIdx(idx);
    }
    // Check constructor calls first to avoid conflicts with other parsers
    if ((stmt = ctx.assertParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.switchParser.parseYield(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.classParser.parseLocalClass(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.constrCallParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.assignParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.postIncrementParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.preIncrementParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.ifParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.doParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.whileParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.forEachParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.forParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.repeatParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.switchParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.throwParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.tryParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.declParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.methodCallParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.retParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.breakParser.parse(tokens)) != null) {
      return stmt;
    } else if ((stmt = ctx.continueParser.parse(tokens)) != null) {
      return stmt;
    } else {
      return parseExpressionStatement(tokens);
    }
  }
}
