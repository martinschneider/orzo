package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.FieldAssignment;
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
    if (!(tokens.curr() instanceof Identifier)) {
      return null;
    }
    int idx = tokens.idx();
    Expression expr = ctx.exprParser.parse(tokens);
    // "<call chain>.field = value;", e.g. "map.get(k).count = 1;" (parsed as an embedded
    // assignment: [target, value..., ASSIGN])
    if (expr != null
        && expr.tokens.size() >= 3
        && expr.tokens.get(expr.tokens.size() - 1).eq(op(ASSIGN))
        && expr.tokens.get(0) instanceof MethodCall
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
    Statement stmt;
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
