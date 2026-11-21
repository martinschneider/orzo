package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CASE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.SWITCH;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchCase;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import java.util.ArrayList;
import java.util.List;

public class SwitchParser implements ProdParser<SwitchStatement> {
  private ParserContext ctx;

  public SwitchParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public SwitchStatement parse(TokenList tokens) {
    if (tokens.curr() == null || !tokens.curr().eq(keyword(SWITCH))) {
      return null;
    }
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
      if (tokens.curr().eq(keyword(CASE))) {
        List<Expression> labels = new ArrayList<>();
        while (tokens.curr() != null && tokens.curr().eq(keyword(CASE))) {
          tokens.next(); // skip 'case'
          Expression label = ctx.exprParser.parse(tokens);
          if (label == null) {
            break;
          }
          if (tokens.curr() != null && tokens.curr().eq(sym(COLON))) {
            tokens.next(); // skip ':'
          }
          labels.add(label);
        }
        List<Statement> body = parseBody(tokens);
        cases.add(new SwitchCase(labels, body));
      } else if (tokens.curr().eq(scope(DEFAULT))) {
        tokens.next(); // skip 'default'
        if (tokens.curr() != null && tokens.curr().eq(sym(COLON))) {
          tokens.next(); // skip ':'
        }
        List<Statement> body = parseBody(tokens);
        cases.add(new SwitchCase(null, body));
      } else {
        break;
      }
    }
    if (tokens.curr() != null && tokens.curr().eq(sym(RBRACE))) {
      tokens.next(); // skip '}'
    }
    return new SwitchStatement(switchExpr, cases);
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
        if (block != null) {
          body.addAll(block);
        }
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
