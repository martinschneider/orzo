package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CATCH;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINALLY;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.TRY;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NOTEQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.CatchBlock;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import java.util.ArrayList;
import java.util.List;

public class TryParser implements ProdParser<TryStatement> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse try stmt";

  public TryParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public TryStatement parse(TokenList tokens) {
    if (!tokens.curr().eq(keyword(TRY))) {
      return null;
    }
    tokens.next(); // consume 'try'
    // try-with-resources: "try (R r = ...; R2 r2 = ...) {...}"
    List<Declaration> resources = new ArrayList<>();
    if (tokens.curr().eq(sym(LPAREN))) {
      tokens.next(); // consume '('
      while (!tokens.curr().eq(sym(RPAREN))) {
        ParallelDeclaration resource = ctx.declParser.parse(tokens);
        if (resource == null) {
          ctx.errors.addError(
              LOG_NAME, "expected resource declaration", new RuntimeException().getStackTrace());
          return null;
        }
        resources.addAll(resource.declarations);
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
      }
      tokens.next(); // consume ')'
    }
    if (!tokens.curr().eq(sym(LBRACE))) {
      ctx.errors.addError(
          LOG_NAME, "expected '{' after try", new RuntimeException().getStackTrace());
      return null;
    }
    tokens.next(); // consume '{'
    List<Statement> tryBody = ctx.stmtParser.parseStmtSeq(tokens);
    if (tryBody == null) {
      tryBody = new ArrayList<>();
    }
    if (!tokens.curr().eq(sym(RBRACE))) {
      ctx.errors.addError(
          LOG_NAME, "expected '}' after try body", new RuntimeException().getStackTrace());
      return null;
    }
    tokens.next(); // consume '}'

    List<CatchBlock> catchBlocks = new ArrayList<>();
    while (tokens.curr().eq(keyword(CATCH))) {
      tokens.next(); // consume 'catch'
      if (!tokens.curr().eq(sym(LPAREN))) {
        ctx.errors.addError(
            LOG_NAME, "expected '(' after catch", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '('
      if (tokens.curr().eq(keyword(FINAL))) {
        tokens.next();
      }
      List<String> exceptionTypes = new ArrayList<>();
      exceptionTypes.add(parseCatchType(tokens));
      while (tokens.curr().eq(op(BITWISE_OR))) { // multi-catch
        tokens.next();
        exceptionTypes.add(parseCatchType(tokens));
      }
      String varName = tokens.curr().val.toString();
      tokens.next(); // consume variable name
      if (!tokens.curr().eq(sym(RPAREN))) {
        ctx.errors.addError(
            LOG_NAME, "expected ')' after catch parameter", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume ')'
      if (!tokens.curr().eq(sym(LBRACE))) {
        ctx.errors.addError(
            LOG_NAME, "expected '{' after catch header", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '{'
      List<Statement> catchBody = ctx.stmtParser.parseStmtSeq(tokens);
      if (catchBody == null) {
        catchBody = new ArrayList<>();
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        ctx.errors.addError(
            LOG_NAME, "expected '}' after catch body", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '}'
      catchBlocks.add(new CatchBlock(exceptionTypes, varName, catchBody));
    }

    List<Statement> finallyBody = null;
    if (tokens.curr().eq(keyword(FINALLY))) {
      tokens.next(); // consume 'finally'
      if (!tokens.curr().eq(sym(LBRACE))) {
        ctx.errors.addError(
            LOG_NAME, "expected '{' after finally", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '{'
      finallyBody = ctx.stmtParser.parseStmtSeq(tokens);
      if (finallyBody == null) {
        finallyBody = new ArrayList<>();
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        ctx.errors.addError(
            LOG_NAME, "expected '}' after finally body", new RuntimeException().getStackTrace());
        return null;
      }
      tokens.next(); // consume '}'
    }

    if (catchBlocks.isEmpty() && finallyBody == null && resources.isEmpty()) {
      ctx.errors.addError(
          LOG_NAME, "try without catch or finally", new RuntimeException().getStackTrace());
      return null;
    }
    // try-with-resources is desugared into nested try-finally blocks, one per resource:
    // "R r = ...; try { <rest> } finally { if (r != null) r.close(); }"
    for (int i = resources.size() - 1; i >= 0; i--) {
      Declaration resource = resources.get(i);
      Identifier name = resource.name;
      TryStatement closing = new TryStatement(tryBody, new ArrayList<>());
      Expression notNull =
          new Expression(List.of(id(name.val.toString()), id("null"), op(NOTEQUAL)));
      List<Statement> closeCall = new ArrayList<>();
      closeCall.add(new MethodCall(name.val.toString() + ".close", new ArrayList<>()));
      List<IfBlock> ifBlocks = new ArrayList<>();
      ifBlocks.add(new IfBlock(notNull, closeCall));
      closing.finallyBody = new ArrayList<>();
      closing.finallyBody.add(new IfStatement(ifBlocks, false));
      tryBody = new ArrayList<>();
      tryBody.add(new ParallelDeclaration(new ArrayList<>(List.of(resource))));
      tryBody.add(closing);
    }
    TryStatement tryStmt = new TryStatement(tryBody, catchBlocks);
    tryStmt.finallyBody = finallyBody;
    return tryStmt;
  }

  // e.g. "IOException" or "java.io.IOException"
  private static String parseCatchType(TokenList tokens) {
    StringBuilder name = new StringBuilder(tokens.curr().val.toString());
    tokens.next();
    while (tokens.curr().eq(sym(DOT))) {
      tokens.next();
      name.append('.').append(tokens.curr().val);
      tokens.next();
    }
    return name.toString();
  }
}
