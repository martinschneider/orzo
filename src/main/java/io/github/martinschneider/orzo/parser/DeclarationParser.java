package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
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
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.ArrayList;
import java.util.List;

public class DeclarationParser implements ProdParser<ParallelDeclaration> {
  private ParserContext ctx;

  public DeclarationParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ParallelDeclaration parse(TokenList tokens) {
    Type type;
    List<Identifier> names = new ArrayList<>();
    List<Byte> arrDims = new ArrayList<>();
    List<Expression> values = new ArrayList<>();
    Declaration decl = null;
    // TODO: this will not work for non-basic types :-(
    if (tokens.curr() instanceof Type && !tokens.curr().eq("VOID")) {
      type = (Type) tokens.curr();
      tokens.next();
      type.arr = parseArrayDef(tokens);
      while (tokens.curr() instanceof Identifier || tokens.curr().eq(sym(COMMA))) {
        if (tokens.curr() instanceof Identifier) {
          Identifier id = (Identifier) tokens.curr();
          tokens.next();
          id.arrSel = ctx.arraySelectorParser.parse(tokens);
          names.add(id);
        } else {
          tokens.next();
        }
      }
      Expression value = null;
      if (tokens.curr().eq(op(ASSIGN))) {
        tokens.next();
        while ((value = parseExpOrArrInit(tokens)) != null || tokens.curr().eq(sym(COMMA))) {
          if (value != null) {
            values.add(value);
            arrDims.add(type.arr);
          } else {
            tokens.next();
          }
        }
      }
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
      List<Declaration> declarations = new ArrayList<>();
      for (int i = 0; i < names.size(); i++) {
        Expression val = (values.size() <= i) ? null : values.get(i);
        byte arrDim = (arrDims.size() <= i) ? 0 : arrDims.get(i);
        declarations.add(new Declaration(type.name, arrDim, names.get(i), val));
      }
      return new ParallelDeclaration(declarations);
    }
    return null;
  }

  Expression parseExpOrArrInit(TokenList tokens) {
    Expression expr = ctx.arrayInitParser.parse(tokens);
    if (expr != null) {
      return expr;
    }
    return ctx.exprParser.parse(tokens);
  }

  byte parseArrayDef(TokenList tokens) {
    byte array = 0;
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
