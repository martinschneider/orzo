package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CASE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.SWITCH;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.THROW;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.ARROW;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.SwitchExpr;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchCase;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A switch statement or expression, with "case X:" or "case X, Y ->" cases. A "->" case doesn't
 * fall through (its statement is followed by a break). A switch expression is a switch statement
 * (see SwitchExpr) whose results ("case X -> value;" and "yield value;") are assigned to a
 * temporary variable.
 */
public class SwitchParser implements ProdParser<SwitchStatement> {
  private ParserContext ctx;
  private int count;
  // the switch expressions being parsed (the innermost first), the targets of "yield"
  private Deque<SwitchExpr> expressions = new ArrayDeque<>();

  public SwitchParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public SwitchStatement parse(TokenList tokens) {
    if (tokens.curr() == null || !tokens.curr().eq(keyword(SWITCH))) {
      return null;
    }
    return parseSwitch(tokens, null);
  }

  /** A switch expression (the current token is "switch"), or null. */
  public SwitchExpr parseExpression(TokenList tokens) {
    int idx = tokens.idx();
    SwitchExpr expr = new SwitchExpr("$switch" + count++, null, new ArrayList<>());
    expressions.push(expr);
    SwitchStatement stmt = parseSwitch(tokens, expr);
    expressions.pop();
    if (stmt == null) {
      tokens.setIdx(idx);
      return null;
    }
    Block block = new Block(new ArrayList<>(List.of(stmt)));
    block.label = expr.var;
    expr.statement = block;
    return expr;
  }

  /** "yield value;" in a switch expression (the current token is "yield"), or null. */
  public Statement parseYield(TokenList tokens) {
    if (expressions.isEmpty()
        || !(tokens.curr() instanceof Identifier)
        || !"yield".equals(tokens.curr().val.toString())
        || ((Identifier) tokens.curr()).next != null) {
      return null;
    }
    int idx = tokens.idx();
    tokens.next();
    Expression value = ctx.exprParser.parse(tokens);
    if (value == null || !tokens.curr().eq(sym(SEMICOLON))) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.next();
    return result(expressions.peek(), value);
  }

  // "$switchN = value; break $switchN;"
  private static Statement result(SwitchExpr expr, Expression value) {
    expr.results.add(value);
    Break leave = new Break();
    leave.label = expr.var;
    Assignment result =
        new Assignment(
            new ArrayList<>(List.of(new Identifier(expr.var))), new ArrayList<>(List.of(value)));
    return new Block(new ArrayList<>(List.of(result, leave)));
  }

  private SwitchStatement parseSwitch(TokenList tokens, SwitchExpr expr) {
    tokens.next(); // skip 'switch'
    if (!tokens.curr().eq(sym(LPAREN))) {
      return null;
    }
    tokens.next(); // skip '('
    Expression switchExpr = ctx.exprParser.parse(tokens);
    if (switchExpr == null) {
      return null;
    }
    if (!tokens.curr().eq(sym(RPAREN))) {
      return null;
    }
    tokens.next(); // skip ')'
    if (!tokens.curr().eq(sym(LBRACE))) {
      return null;
    }
    tokens.next(); // skip '{'

    List<SwitchCase> cases = new ArrayList<>();
    while (tokens.curr() != null && !tokens.curr().eq(sym(RBRACE))) {
      List<Expression> labels = null;
      if (tokens.curr().eq(keyword(CASE))) {
        labels = new ArrayList<>();
        while (tokens.curr() != null && tokens.curr().eq(keyword(CASE))) {
          tokens.next(); // skip 'case'
          ctx.inCaseLabel = true;
          Expression label = ctx.exprParser.parse(tokens);
          while (label != null) {
            labels.add(label);
            label = null;
            if (tokens.curr().eq(sym(COMMA))) { // e.g. "case 1, 2"
              tokens.next();
              label = ctx.exprParser.parse(tokens);
            }
          }
          ctx.inCaseLabel = false;
          if (tokens.curr() != null && tokens.curr().eq(sym(COLON))) {
            tokens.next(); // skip ':'
          } else {
            break;
          }
        }
      } else if (tokens.curr().eq(scope(DEFAULT))) {
        tokens.next(); // skip 'default'
        if (tokens.curr() != null && tokens.curr().eq(sym(COLON))) {
          tokens.next(); // skip ':'
        }
      } else {
        break;
      }
      List<Statement> body;
      if (tokens.curr().eq(sym(ARROW))) {
        tokens.next();
        body = parseArrowBody(tokens, expr);
        if (body == null) {
          break;
        }
      } else {
        body = parseBody(tokens);
      }
      cases.add(new SwitchCase(labels, body));
    }
    if (tokens.curr() != null && tokens.curr().eq(sym(RBRACE))) {
      tokens.next(); // skip '}'
    }
    return new SwitchStatement(switchExpr, cases);
  }

  // The statement after "case X ->", followed by a break (in a switch expression, a value is its
  // result, a block or a throw statement must yield or throw)
  private List<Statement> parseArrowBody(TokenList tokens, SwitchExpr expr) {
    List<Statement> body = new ArrayList<>();
    if (expr != null && !tokens.curr().eq(sym(LBRACE)) && !tokens.curr().eq(keyword(THROW))) {
      Expression value = ctx.exprParser.parse(tokens);
      if (value == null) {
        return null;
      }
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      } else {
        StatementParser.reportMissingSemicolon(ctx, tokens);
      }
      body.add(result(expr, value));
      return body;
    }
    Statement stmt = ctx.stmtParser.parse(tokens);
    if (stmt == null) {
      return null;
    }
    body.add(stmt);
    body.add(new Break());
    return body;
  }

  private List<Statement> parseBody(TokenList tokens) {
    List<Statement> body = new ArrayList<>();
    while (tokens.curr() != null
        && !tokens.curr().eq(keyword(CASE))
        && !tokens.curr().eq(scope(DEFAULT))
        && !tokens.curr().eq(sym(RBRACE))) {
      if (tokens.curr().eq(sym(LBRACE))) {
        // a braced block, e.g. "case X: { ... }"
        tokens.next();
        List<Statement> block = ctx.stmtParser.parseStmtSeq(tokens);
        // (a block of its own: its variables are only in scope in it)
        body.add(new Block((block != null) ? block : new ArrayList<>()));
        if (!tokens.curr().eq(sym(RBRACE))) {
          break;
        }
        tokens.next();
        continue;
      }
      Statement stmt = ctx.stmtParser.parse(tokens);
      if (stmt == null) {
        break;
      }
      body.add(stmt);
    }
    return body;
  }
}
