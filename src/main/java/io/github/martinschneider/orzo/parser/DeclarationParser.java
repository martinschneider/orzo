package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
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
    int idx = tokens.idx();
    Type type = null;
    List<Identifier> names = new ArrayList<>();
    List<Byte> arrDims = new ArrayList<>();
    List<Expression> values = new ArrayList<>();
    List<AccessFlag> accFlags = new ArrayList<>();
    if (tokens.curr() instanceof Scope) {
      try {
        accFlags.add(((Scopes) ((Scope) tokens.curr()).val).accFlag);
      } catch (NoSuchFieldError e) {
        // TODO: Temporary workaround for bootstrapping circular dependency
        // Handle case where Scopes.accFlag field doesn't exist yet (during self-compilation)
        // Use a default access flag based on the scope name
        // This should be replaced with proper dependency resolution and compilation ordering
        Scopes scopeVal = (Scopes) ((Scope) tokens.curr()).val;
        switch (scopeVal) {
          case PUBLIC:
            accFlags.add(AccessFlag.ACC_PUBLIC);
            break;
          case PRIVATE:
            accFlags.add(AccessFlag.ACC_PRIVATE);
            break;
          case PROTECTED:
            accFlags.add(AccessFlag.ACC_PROTECTED);
            break;
          case DEFAULT:
            // No access flag needed for default
            break;
        }
      }
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
    if (tokens.curr() instanceof Type && !tokens.curr().eq("VOID")) {
      type = (Type) tokens.curr();
    } else if (tokens.curr() instanceof Identifier
        && ctx.typeMap.TYPES.containsKey(tokens.curr().toString())) {
      type = ctx.typeMap.TYPES.get(tokens.curr().toString());
    } else if (tokens.curr() instanceof Identifier
        && ctx.importMap.containsKey(tokens.curr().toString())) {
      type = new Type(ctx.importMap.get(tokens.curr().toString()));
    } else if (tokens.curr() instanceof Identifier
        && !tokens.curr().toString().isEmpty()
        && Character.isUpperCase(tokens.curr().toString().charAt(0))) {
      String id = tokens.curr().toString();
      String fqn;
      try {
        Class.forName("java.lang." + id);
        fqn = "java.lang." + id;
      } catch (ClassNotFoundException e) {
        fqn =
            (ctx.currClazz != null
                    && ctx.currClazz.packageName != null
                    && !ctx.currClazz.packageName.isEmpty())
                ? ctx.currClazz.packageName + "." + id
                : id;
      }
      type = new Type(fqn);
    }
    if (type != null) {
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
      // Skip generic type arguments e.g. List<Statement>
      if (tokens.curr().eq(op(LESS))) {
        int depth = 1;
        while (depth > 0) {
          tokens.next();
          if (tokens.curr().eq(op(LESS))) depth++;
          else if (tokens.curr().eq(op(GREATER))) depth--;
          else if (tokens.curr().eq(op(RSHIFT))) depth = Math.max(0, depth - 2);
          else if (tokens.curr().eq(op(RSHIFTU))) depth = Math.max(0, depth - 3);
        }
        tokens.next();
      }
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
      if (names.isEmpty()) {
        tokens.setIdx(idx);
        return null;
      }
      List<Declaration> declarations = new ArrayList<>();
      for (int i = 0; i < names.size(); i++) {
        Expression val = (values.size() <= i) ? null : values.get(i);
        declarations.add(new Declaration(accFlags, type.name, type.arr, names.get(i), val));
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
