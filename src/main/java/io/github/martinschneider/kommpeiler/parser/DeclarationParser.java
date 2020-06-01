package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Type;

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
