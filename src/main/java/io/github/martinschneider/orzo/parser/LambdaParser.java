package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.EXTENDS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FINAL;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.NEW;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.ARROW;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.QUESTION;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.LambdaExpr;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

// A lambda ("x -> expr", "(x, y) -> expr") or a method reference ("Math::min") as a single
// LambdaExpr token. Only untyped parameters and an expression body are supported.
public class LambdaParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse lambda expression";

  public LambdaParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Expression parse(TokenList tokens) {
    if (ctx.inCaseLabel) {
      return null; // (e.g. "case A -> ..." isn't a lambda)
    }
    int idx = tokens.idx();
    LambdaExpr ref = parseMethodRef(tokens);
    if (ref != null) {
      List<Token> refTokens = new ArrayList<>();
      refTokens.add(ref);
      return new Expression(refTokens);
    }
    List<String> params = tryParseParams(tokens);
    if (params == null || !tokens.curr().eq(sym(ARROW))) {
      tokens.setIdx(idx);
      return null;
    }
    // (the field is overwritten by the lambdas in the body)
    List<String> paramTypes = this.paramTypes;
    tokens.next(); // consume '->'
    if (tokens.curr().eq(sym(LBRACE))) {
      // a block body
      tokens.next();
      List<Statement> block = ctx.stmtParser.parseStmtSeq(tokens);
      if (tokens.curr().eq(sym(RBRACE))) {
        tokens.next();
      } else {
        ctx.errors.missingExpected(
            LOG_NAME, sym(RBRACE), tokens, new RuntimeException().getStackTrace());
      }
      LambdaExpr lambda = new LambdaExpr(params, new Expression(new ArrayList<>()));
      lambda.block = (block != null) ? block : new ArrayList<>();
      lambda.paramTypes = paramTypes;
      List<Token> lambdaTokens = new ArrayList<>();
      lambdaTokens.add(lambda);
      return new Expression(lambdaTokens);
    }
    Expression body = ctx.exprParser.parse(tokens);
    if (body == null) {
      ctx.errors.addError(LOG_NAME, "missing lambda body", new RuntimeException().getStackTrace());
      tokens.setIdx(idx);
      return null;
    }
    List<Token> bodyTokens = new ArrayList<>();
    LambdaExpr lambda = new LambdaExpr(params, body);
    lambda.paramTypes = paramTypes;
    bodyTokens.add(lambda);
    return new Expression(bodyTokens);
  }

  // Recognises either a bare identifier ("x") or a parenthesized, comma-separated, untyped
  // identifier list ("()", "(x)", "(x, y)") as a lambda parameter list. Returns null (and leaves
  // `tokens` untouched) if the upcoming tokens don't match either shape - this does NOT by itself
  // confirm a lambda (an "->" could still be missing after it, e.g. a plain parenthesized
  // expression "(x)"), so the caller still checks for "->" before committing.
  // A method reference, e.g. "Math::min", "String::length", "Map.Entry::getKey" or "Foo::new"
  // (null with the tokens unchanged if there's none)
  private LambdaExpr parseMethodRef(TokenList tokens) {
    int idx = tokens.idx();
    if (tokens.curr().eq(keyword(NEW)) || tokens.curr().eq(sym(LPAREN))) {
      return parseReceiverMethodRef(tokens);
    }
    if (!((tokens.curr() instanceof Identifier && ((Identifier) tokens.curr()).next == null)
        || tokens.curr() instanceof Type)) {
      return null;
    }
    StringBuilder owner =
        new StringBuilder(
            (tokens.curr() instanceof Type)
                ? ((Type) tokens.curr()).name
                : ((Identifier) tokens.curr()).id());
    int i = idx + 1;
    while (tokens.get(i).eq(sym(DOT))
        && (tokens.get(i + 1) instanceof Identifier || tokens.get(i + 1) instanceof Type)) {
      Token segment = tokens.get(i + 1);
      owner.append('.').append((segment instanceof Type) ? ((Type) segment).name : segment.val);
      i += 2;
    }
    i = skipTypeArguments(tokens, i); // e.g. "ArrayList<String>::new" (erased)
    while (tokens.get(i).eq(sym(LBRAK)) && tokens.get(i + 1).eq(sym(RBRAK))) {
      owner.append("[]"); // an array type, e.g. "int[]::new"
      i += 2;
    }
    Token name = tokens.get(i + 2);
    if (!tokens.get(i).eq(sym(COLON))
        || !tokens.get(i + 1).eq(sym(COLON))
        || !(name instanceof Identifier || name.eq(keyword(NEW)))) {
      return null;
    }
    tokens.setIdx(i + 3);
    String ownerName = owner.toString();
    if (ownerName.contains(".") && !ownerName.startsWith("this.") && !ownerName.endsWith("[]")) {
      // e.g. "java.util.Map$Entry" for "Map.Entry"
      ownerName = ctx.typeArgsParser.resolveQualified(ownerName);
    }
    return LambdaExpr.methodRef(
        ownerName, name.eq(keyword(NEW)) ? "new" : ((Identifier) name).id());
  }

  // Whether the "(" is followed by "->" after its ")" (only then the parameters are parsed: their
  // types are parsed with error reporting, e.g. "a" in "(a < b ? a : b)" isn't a type)
  private static boolean followedByArrow(TokenList tokens) {
    int depth = 0;
    for (int i = tokens.idx(); i < tokens.list().size(); i++) {
      Token token = tokens.get(i);
      if (token.eq(sym(LPAREN))) {
        depth++;
      } else if (token.eq(sym(RPAREN)) && --depth == 0) {
        return i + 1 < tokens.list().size() && tokens.get(i + 1).eq(sym(ARROW));
      }
    }
    return false;
  }

  // the declared types of the parameters of the lambda being parsed (null for an inferred one)
  private List<String> paramTypes;

  // the type of an explicitly typed parameter, e.g. "String" or "java.util.List" (erased) in
  // "(String s, java.util.List<T> l) -> ...", null if there is none
  private String parseParamType(TokenList tokens) {
    Token first = tokens.curr();
    Token next = tokens.peekNext();
    if (!(first instanceof Identifier || first instanceof Type)
        || next.eq(sym(COMMA))
        || next.eq(sym(RPAREN))) {
      return null;
    }
    StringBuilder type =
        new StringBuilder((first instanceof Type) ? ((Type) first).name : first.val.toString());
    tokens.next();
    while (tokens.curr().eq(sym(DOT)) && tokens.peekNext() instanceof Identifier) {
      tokens.next();
      type.append('.').append(tokens.curr().val);
      tokens.next();
    }
    if (tokens.curr().eq(op(LESS))) {
      ctx.typeArgsParser.parse(tokens);
    }
    while (tokens.curr().eq(sym(LBRAK)) && tokens.peekNext().eq(sym(RBRAK))) {
      tokens.next();
      tokens.next();
      type.insert(0, '[');
    }
    return type.toString();
  }

  // The index after type arguments starting at the given index (e.g. "<String, List<T>>"), or the
  // given index if there are none
  private static int skipTypeArguments(TokenList tokens, int start) {
    if (!tokens.get(start).eq(op(LESS))) {
      return start;
    }
    int depth = 0;
    int i = start;
    do {
      Token token = tokens.get(i);
      if (token.eq(op(LESS))) {
        depth++;
      } else if (token.eq(op(GREATER))) {
        depth--;
      } else if (token.eq(op(RSHIFT))) {
        depth -= 2;
      } else if (token.eq(op(RSHIFTU))) {
        depth -= 3;
      } else if (!(token instanceof Identifier
          || token instanceof Type
          || token.eq(sym(DOT))
          || token.eq(sym(COMMA))
          || token.eq(sym(QUESTION))
          || token.eq(sym(LBRAK))
          || token.eq(sym(RBRAK))
          || token.eq(keyword(EXTENDS)))) {
        return start; // (not type arguments, e.g. "a < b")
      }
      i++;
    } while (depth > 0);
    return i;
  }

  // A method reference on an object creation or a parenthesized expression, e.g. "new Foo()::bar"
  // or "(a ? b : c)::bar" (only parsed if a "::" follows, see endOfReceiver)
  private LambdaExpr parseReceiverMethodRef(TokenList tokens) {
    int end = endOfReceiver(tokens, tokens.idx());
    if (end < 0
        || !tokens.get(end).eq(sym(COLON))
        || !tokens.get(end + 1).eq(sym(COLON))
        || !(tokens.get(end + 2) instanceof Identifier)) {
      return null;
    }
    int idx = tokens.idx();
    receiverStart = idx;
    Expression receiver = ctx.exprParser.parse(tokens);
    receiverStart = -1;
    if (receiver == null || tokens.idx() != end) {
      tokens.setIdx(idx);
      return null;
    }
    tokens.setIdx(end + 3);
    LambdaExpr ref = LambdaExpr.methodRef(null, ((Identifier) tokens.get(end + 2)).id());
    ref.methodRefReceiver = receiver;
    return ref;
  }

  // (the start of the receiver being parsed: its "::" isn't parsed again there)
  private int receiverStart = -1;

  // The index after "(...)" or "new Foo<...>(...)" at the given index, or -1
  private int endOfReceiver(TokenList tokens, int start) {
    if (start == receiverStart) {
      return -1;
    }
    int i = start;
    if (tokens.get(i).eq(keyword(NEW))) {
      i++;
      while (tokens.get(i) instanceof Identifier || tokens.get(i).eq(sym(DOT))) {
        i++;
      }
      i = skipTypeArguments(tokens, i);
    }
    if (!tokens.get(i).eq(sym(LPAREN))) {
      return -1;
    }
    for (int depth = 0; !(tokens.get(i) instanceof EOF); i++) {
      if (tokens.get(i).eq(sym(LPAREN))) {
        depth++;
      } else if (tokens.get(i).eq(sym(RPAREN)) && --depth == 0) {
        return i + 1;
      }
    }
    return -1;
  }

  private List<String> tryParseParams(TokenList tokens) {
    int idx = tokens.idx();
    paramTypes = null;
    if (tokens.curr() instanceof Identifier && ((Identifier) tokens.curr()).next == null) {
      List<String> params = new ArrayList<>();
      params.add(((Identifier) tokens.curr()).id());
      tokens.next();
      return params;
    }
    if (!tokens.curr().eq(sym(LPAREN)) || !followedByArrow(tokens)) {
      return null;
    }
    tokens.next(); // consume '('
    List<String> params = new ArrayList<>();
    if (tokens.curr().eq(sym(RPAREN))) {
      tokens.next();
      return params;
    }
    paramTypes = new ArrayList<>();
    while (true) {
      if (tokens.curr().eq(keyword(FINAL))) {
        tokens.next();
      }
      String type = parseParamType(tokens);
      Token t = tokens.curr();
      if (!(t instanceof Identifier) || ((Identifier) t).next != null) {
        tokens.setIdx(idx);
        return null;
      }
      params.add(((Identifier) t).id());
      paramTypes.add(Type.VAR.equals(type) ? null : type); // ("var x" is an implicit type)
      tokens.next();
      if (tokens.curr().eq(sym(COMMA))) {
        tokens.next();
        continue;
      }
      if (tokens.curr().eq(sym(RPAREN))) {
        tokens.next();
        return params;
      }
      tokens.setIdx(idx);
      return null;
    }
  }
}
