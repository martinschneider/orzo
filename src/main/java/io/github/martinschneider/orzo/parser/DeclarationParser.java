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
    Expression value;
    // TODO: this will not work for non-basic types :-(
    if (tokens.curr() instanceof Type && !tokens.curr().eq("VOID")) {
      type = (Type) tokens.curr();
      tokens.next();
      type.setArray(parseArrayDef(tokens));
      if (tokens.curr() instanceof Identifier) {
        name = (Identifier) tokens.curr();
        tokens.next();
        if (tokens.curr().eq(op(ASSIGN))) {
          tokens.next();
          if (type.getArray() > 0 && (value = ctx.arrayInitParser.parse(tokens)) != null) {
            return new Declaration(type.getName(), type.getArray(), name, value, true);
          }
          if ((value = ctx.exprParser.parse(tokens)) != null) {
            if (tokens.curr().eq(sym(SEMICOLON))) {
              tokens.next();
            }
            return new Declaration(type.getName(), type.getArray(), name, value, true);
          }
          tokens.prev();
        }
        if (tokens.curr().eq(sym(SEMICOLON))) {
          tokens.next();
        }
        return new Declaration(type.getName(), name, null, false);
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
