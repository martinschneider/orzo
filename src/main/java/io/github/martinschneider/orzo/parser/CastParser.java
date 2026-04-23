package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Type;

public class CastParser implements ProdParser<Type> {

  private ParserContext ctx;

  public CastParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Type parse(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(sym(LPAREN))) {
      tokens.next();
      Type type = null;
      if (tokens.curr() instanceof Type) {
        type = (Type) tokens.curr();
        tokens.next();
      } else if (tokens.curr() instanceof Identifier) {
        String name = tokens.curr().toString();
        if (!name.isEmpty() && Character.isUpperCase(name.charAt(0))) {
          // User-defined class cast like (Argument) or (io.example.Foo)
          String fqn = ctx.importMap.get(name);
          if (fqn == null && ctx.currClazz != null && name.equals(ctx.currClazz.name)) {
            fqn = ctx.currClazz.fqn();
          }
          if (fqn == null) {
            fqn = name;
          }
          type = new Type(fqn);
          tokens.next();
        }
      }
      if (type == null) {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
      if (tokens.curr().eq(sym(RPAREN))) {
        tokens.next();
        return type;
      } else {
        ctx.errors.tokenIdx = tokens.idx();
        tokens.setIdx(idx);
        return null;
      }
    }
    return null;
  }
}
