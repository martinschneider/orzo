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
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.FieldAssignment;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.LoopStatement;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class StatementParser implements ProdParser<Statement> {
  private ParserContext ctx;

  public StatementParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  public List<Statement> parseStmtSeq(TokenList tokens) {
    List<Statement> stmtSequence = new ArrayList<>();
    Statement stmt;
    int lastIdx = -1;
    int stuckCount = 0;
    while ((stmt = parse(tokens)) != null) {
      stmtSequence.add(stmt);
      int currIdx = tokens.idx();
      if (currIdx == lastIdx) {
        stuckCount++;
        if (stuckCount >= 3) {
          System.err.println(
              "[LOOP GUARD] parseStmtSeq stuck at token idx="
                  + currIdx
                  + " token="
                  + tokens.curr()
                  + " parsed="
                  + stmt.getClass().getSimpleName());
          break;
        }
      } else {
        stuckCount = 0;
      }
      lastIdx = currIdx;
    }
    return (stmtSequence.size() == 0) ? null : stmtSequence;
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

  @Override
  public Statement parse(TokenList tokens) {
    Token start = tokens.curr();
    Statement stmt = parseStatement(tokens);
    if (stmt != null) {
      // (for the position of errors when generating its code)
      ctx.errors.startTokens.putIfAbsent(stmt, start);
    }
    return stmt;
  }

  private Statement parseStatement(TokenList tokens) {
    Statement stmt;
    if (tokens.curr().eq(sym(LBRACE))) {
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
    if ((stmt = ctx.constrCallParser.parse(tokens)) != null) {
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
