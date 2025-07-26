package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.util.FactoryHelper.id;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;

import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.ArrayList;
import java.util.List;

public class EnumParser implements ProdParser<ParallelDeclaration> {

  public String fqn;
  public ParserContext ctx;

  public EnumParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  public ParallelDeclaration parse(TokenList tokens) {
    List<Declaration> decls = new ArrayList<>();
    if (tokens.curr() instanceof Identifier) {
      // Parse enum constant with optional constructor arguments
      String enumConstName = tokens.curr().toString();
      Expression constArgs = null;

      tokens.next();
      // Check if this enum constant has constructor arguments
      if (tokens.curr().eq(sym(LPAREN))) {
        tokens.next();
        if (!tokens.curr().eq(sym(RPAREN))) {
          // Parse constructor arguments - for now, we'll store as a simple expression
          constArgs = ctx.exprParser.parse(tokens);
        }
        if (!tokens.curr().eq(sym(RPAREN))) {
          ctx.errors.addError("enum parser", "missing )", new RuntimeException().getStackTrace());
        }
        tokens.next();
      }

      decls.add(
          new Declaration(
              list(AccessFlag.ACC_PUBLIC, AccessFlag.ACC_STATIC, AccessFlag.ACC_FINAL),
              TypeUtils.descr(fqn),
              id(enumConstName),
              constArgs));

      // Parse additional enum constants
      while (tokens.curr().eq(sym(COMMA))) {
        tokens.next();
        if (tokens.curr() instanceof Identifier) {
          enumConstName = tokens.curr().toString();
          constArgs = null;
          tokens.next();

          // Check if this enum constant has constructor arguments
          if (tokens.curr().eq(sym(LPAREN))) {
            tokens.next();
            if (!tokens.curr().eq(sym(RPAREN))) {
              constArgs = ctx.exprParser.parse(tokens);
            }
            if (!tokens.curr().eq(sym(RPAREN))) {
              ctx.errors.addError(
                  "enum parser", "missing )", new RuntimeException().getStackTrace());
            }
            tokens.next();
          }

          decls.add(
              new Declaration(
                  list(AccessFlag.ACC_PUBLIC, AccessFlag.ACC_STATIC, AccessFlag.ACC_FINAL),
                  TypeUtils.descr(fqn),
                  id(enumConstName),
                  constArgs));
        }
      }

      // Optional semicolon after enum constants
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
    }
    return new ParallelDeclaration(decls);
  }
}
