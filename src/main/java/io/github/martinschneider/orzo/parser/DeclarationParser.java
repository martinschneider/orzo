package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;

public class DeclarationParser implements ProdParser<Declaration> {
  private ParserContext ctx;

  public DeclarationParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Declaration parse(TokenList tokens) {
    Type type;
    Identifier name;
    Expression val;
    // TODO: this will not work for non-basic types :-(
    if (tokens.curr() instanceof Type && !tokens.curr().eq("VOID")) {
      type = (Type) tokens.curr();
      tokens.next();
      type.arr = parseArrayDef(tokens);
      if (tokens.curr() instanceof Identifier) {
        name = (Identifier) tokens.curr();
        tokens.next();
        if (tokens.curr().eq(op(ASSIGN))) {
          tokens.next();
          if (type.arr > 0 && (val = ctx.arrayInitParser.parse(tokens)) != null) {
            return new Declaration(type.name, type.arr, name, val, true);
          }
          if ((val = ctx.exprParser.parse(tokens)) != null) {
            if (tokens.curr().eq(sym(SEMICOLON))) {
              tokens.next();
            }
            return new Declaration(type.name, type.arr, name, val, true);
          }
          tokens.prev();
        }
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
        return new Declaration(type.name, name, null, false);
      } else {
        tokens.prev();
      }
    }
    return null;
  }

  int parseArrayDef(TokenList tokens) {
    int array = 0;
    while (tokens.curr().eq(sym(LBRAK))) {
      tokens.next();
      if (tokens.curr().eq(sym(RBRAK))) {
        array++;
      } else {
        return 0;
      }
      tokens.next();
    }
    return array;
  }
}
