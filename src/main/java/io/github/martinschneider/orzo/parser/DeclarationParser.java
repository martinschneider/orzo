package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.ArrayList;
import java.util.List;

public class DeclarationParser implements ProdParser<ParallelDeclaration> {
  private static final String LOG_NAME = "parse declaration";
  private ParserContext ctx;

  public DeclarationParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  private String resolveWithWildcards(String id) {
    if (ctx.currClazz == null || ctx.currClazz.imports == null) {
      return null;
    }
    for (Import imp : ctx.currClazz.imports) {
      if (!imp.isStatic && imp.id != null && imp.id.endsWith(".*")) {
        String pkg = imp.id.substring(0, imp.id.length() - 2);
        String fqn = pkg + "." + id;
        try {
          Class.forName(fqn);
          return fqn;
        } catch (ClassNotFoundException e) {
          // not in this wildcard package
        }
      }
    }
    return null;
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
      accFlags.add(((Scopes) ((Scope) tokens.curr()).val).accFlag);
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
        && ctx.importMap.containsKey(tokens.curr().toString())
        && !ctx.typeArgsParser.isQualifiedName(tokens)) {
      type = new Type(ctx.importMap.get(tokens.curr().toString()));
    } else if (tokens.curr() instanceof Identifier && tokens.peekNext().eq(sym(DOT))) {
      // Fully-qualified type name, e.g.
      // "io.github.martinschneider.orzo.parser.productions.Argument".
      // Scan the dotted chain; if the last segment starts uppercase, treat the whole thing as an
      // already-qualified type and use it as-is. Otherwise backtrack — this wasn't a type at all
      // (e.g. a dotted expression in a position this branch can't tell apart from a declaration).
      int scanStart = tokens.idx();
      StringBuilder fqn = new StringBuilder(tokens.curr().toString());
      boolean validChain = true;
      while (tokens.peekNext().eq(sym(DOT))) {
        int beforeDot = tokens.idx();
        tokens.next();
        // The last segment of a built-in-sounding FQN (e.g. "java.lang.String") lexes as a
        // Type token, not an Identifier — Type.BASIC_TYPES includes "String" alongside the
        // primitives.
        if (!(tokens.peekNext() instanceof Identifier) && !(tokens.peekNext() instanceof Type)) {
          tokens.setIdx(beforeDot);
          validChain = false;
          break;
        }
        tokens.next();
        fqn.append('.').append(tokens.curr().toString());
      }
      String lastSegment = tokens.curr().toString();
      if (validChain && !lastSegment.isEmpty() && Character.isUpperCase(lastSegment.charAt(0))) {
        type = new Type(ctx.typeArgsParser.resolveQualified(fqn.toString()));
      } else {
        tokens.setIdx(scanStart);
      }
    } else if (tokens.curr() instanceof Identifier
        && ctx.classTypeParams.containsKey(tokens.curr().toString())
        && ((Identifier) tokens.curr()).next == null) {
      // a type variable, e.g. "T value": erased to its bound (like parameter and return types)
      type = new Type(ctx.classTypeParams.get(tokens.curr().toString()));
    } else if (tokens.curr() instanceof Identifier
        && !tokens.curr().toString().isEmpty()
        && Character.isUpperCase(tokens.curr().toString().charAt(0))) {
      String id = tokens.curr().toString();
      String fqn = resolveWithWildcards(id);
      if (fqn == null) {
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
      }
      type = new Type(fqn);
    }
    if (type != null) {
      tokens.next();
      type.arr = ctx.arrayDefParser.parse(tokens);
      // Generic type arguments e.g. List<Statement>, kept alongside the erased type
      String typeArgs = null;
      if (tokens.curr().eq(op(LESS))) {
        typeArgs = ctx.typeArgsParser.parse(tokens);
        if (type.arr == 0) {
          type.arr = ctx.arrayDefParser.parse(tokens); // e.g. TypeVariable<?>[] params
        }
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
        // (parallel declarations: values separated by commas, e.g. "int a, b = 1, 2;")
        while ((value = parseExpOrArrInit(tokens, type)) != null) {
          values.add(value);
          arrDims.add(type.arr);
          if (!tokens.curr().eq(sym(COMMA))) {
            break;
          }
          tokens.next();
        }
        if (values.isEmpty() && !names.isEmpty()) {
          ctx.errors.addError(
              LOG_NAME,
              "expected an expression but found " + tokens.curr().display(),
              new RuntimeException().getStackTrace());
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
        Declaration decl = new Declaration(accFlags, type.name, type.arr, names.get(i), val);
        if (typeArgs != null && type.arr == 0) {
          decl.genericType = type.name.replace('/', '.') + typeArgs;
        }
        declarations.add(decl);
      }
      return new ParallelDeclaration(declarations);
    }
    return null;
  }

  Expression parseExpOrArrInit(TokenList tokens, Type type) {
    if (type.arr == 1 && tokens.curr().eq(sym(LBRACE))) {
      // array initializer shorthand, e.g. "int[] a = {1, 2};"
      int idx = tokens.idx();
      tokens.next();
      List<Expression> vals = new ArrayList<>();
      Expression val;
      while ((val = ctx.exprParser.parse(tokens)) != null) {
        vals.add(val);
        if (tokens.curr().eq(sym(COMMA))) {
          tokens.next();
        }
      }
      if (!tokens.curr().eq(sym(RBRACE))) {
        tokens.setIdx(idx);
        return null;
      }
      tokens.next();
      List<Expression> dims = new ArrayList<>();
      dims.add(new Expression(new ArrayList<>(List.of(new IntLiteral(vals.size(), false)))));
      List<List<Expression>> allVals = new ArrayList<>();
      allVals.add(vals);
      return new ArrayInit(type.name, dims, allVals);
    }
    return parseExpOrArrInit(tokens);
  }

  Expression parseExpOrArrInit(TokenList tokens) {
    Expression expr = ctx.arrayInitParser.parse(tokens);
    if (expr != null) {
      return expr;
    }
    return ctx.exprParser.parse(tokens);
  }
}
