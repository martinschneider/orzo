package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.util.FactoryHelper.id;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.ArrayList;
import java.util.List;

public class EnumParser implements ProdParser<ParallelDeclaration> {

  private static final String LOG_NAME = "parse enum";
  public String fqn;
  public ParserContext ctx;

  public EnumParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  // Enum constants, e.g. "RED, GREEN(1, "g");"
  public ParallelDeclaration parse(TokenList tokens) {
    List<Declaration> decls = new ArrayList<>();
    if (tokens.curr() instanceof Identifier) {
      decls.add(parseConstant(tokens));
      while (tokens.curr().eq(sym(COMMA)) && tokens.peekNext() instanceof Identifier) {
        tokens.next();
        decls.add(parseConstant(tokens));
      }
      if (tokens.curr().eq(sym(COMMA))) {
        tokens.next(); // a trailing comma, e.g. "RED, GREEN,;"
      }
    }
    if (decls.isEmpty() && tokens.curr().eq(sym(COMMA))) {
      tokens.next(); // no constants, e.g. "enum E { , }"
    }
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    return new ParallelDeclaration(decls);
  }

  // "NAME" or "NAME(arg, ...)"
  private Declaration parseConstant(TokenList tokens) {
    String name = tokens.curr().toString();
    tokens.next();
    List<Expression> args = new ArrayList<>();
    if (tokens.curr().eq(sym(LPAREN))) {
      tokens.next();
      Expression arg;
      while ((arg = ctx.exprParser.parse(tokens)) != null) {
        args.add(arg);
        if (!tokens.curr().eq(sym(COMMA))) {
          break;
        }
        tokens.next();
      }
      if (tokens.curr().eq(sym(RPAREN))) {
        tokens.next();
      } else {
        ctx.errors.missingExpected(
            LOG_NAME, sym(RPAREN), tokens, new RuntimeException().getStackTrace());
      }
    }
    Declaration constant =
        new Declaration(
            list(AccessFlag.ACC_PUBLIC, AccessFlag.ACC_STATIC, AccessFlag.ACC_FINAL),
            fqn,
            id(name),
            null);
    constant.enumArgs = args;
    return constant;
  }
}
