package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keyword.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keyword.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.TYPE;

import java.util.ArrayList;
import java.util.List;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.BasicType;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Identifier;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Type;

public class DeclarationParser implements ProdParser<ParallelDeclaration> {
  private ParserContext ctx;

  public DeclarationParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ParallelDeclaration parse(TokenList tokens) {
    Type type = null;
    List<Identifier> names = new ArrayList<>();
    List<Byte> arrDims = new ArrayList<>();
    List<Expression> values = new ArrayList<>();
    List<AccessFlag> accFlags = new ArrayList<>();
    if (tokens.curr().isScope()) {
      accFlags.add(tokens.curr().scopeVal().accFlag); // OMG!
      tokens.next();
    }
    if (tokens.curr().eq(keyword(STATIC))) {
      accFlags.add(AccessFlag.ACC_STATIC);
      tokens.next();
    }
    if (tokens.curr().eq(keyword(FINAL))) {
      accFlags.add(AccessFlag.ACC_FINAL);
      tokens.next();
    }
    if (tokens.curr().eq(TYPE, BasicType.VOID)) {
      type = Type.of(tokens.curr().val);
    } else if (tokens.curr().isId()
        && ctx.typeMap.TYPES.containsKey(tokens.curr().toString())) {
      type = ctx.typeMap.TYPES.get(tokens.curr().toString());
    }
    if (type != null) {
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
      while (tokens.curr().isId() || tokens.curr().eq(sym(COMMA))) {
        if (tokens.curr().isId()) {
          Identifier id = Identifier.of(tokens.curr().val);
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
        declarations.add(new Declaration(accFlags, type.name, arrDim, names.get(i), val));
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
}
