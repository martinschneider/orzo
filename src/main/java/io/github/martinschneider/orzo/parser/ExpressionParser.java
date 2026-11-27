package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.INSTANCEOF;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.NEW;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_NOT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.CAST;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NEGATE;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LFLOOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.QUESTION;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RFLOOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SQRT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.tree.BinaryNode;
import io.github.martinschneider.orzo.parser.tree.CallNode;
import io.github.martinschneider.orzo.parser.tree.CastNode;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.IndexNode;
import io.github.martinschneider.orzo.parser.tree.InstanceOfNode;
import io.github.martinschneider.orzo.parser.tree.LiteralNode;
import io.github.martinschneider.orzo.parser.tree.NameNode;
import io.github.martinschneider.orzo.parser.tree.NewNode;
import io.github.martinschneider.orzo.parser.tree.OpaqueNode;
import io.github.martinschneider.orzo.parser.tree.PostfixLowering;
import io.github.martinschneider.orzo.parser.tree.SelectNode;
import io.github.martinschneider.orzo.parser.tree.TernaryNode;
import io.github.martinschneider.orzo.parser.tree.UnaryNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses an expression into a tree (see parser.tree) by precedence climbing: e.g. "a + b * c" into
 * the BinaryNode "+" of "a" and "b * c". The binary operators are left-associative (also "**"),
 * except for the assignments. The tokens aren't modified (so a caller can backtrack).
 *
 * <p>parse() returns the expression in the postfix form the code generator works with (see
 * PostfixLowering).
 */
public class ExpressionParser implements ProdParser<Expression> {
  private static final String LOG_NAME = "parse expression";
  // the precedence of "a ? b : c" (between the assignments and "||") and of instanceof (that of
  // the relational operators)
  private static final int TERNARY = 2;
  private static final int RELATIONAL = 9;
  private ParserContext ctx;
  private PostfixLowering lowering = new PostfixLowering();

  public ExpressionParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  /** An expression in postfix form, or null (with the tokens unchanged) if there is none. */
  @Override
  public Expression parse(TokenList tokens) {
    ExprNode tree = parseTree(tokens);
    if (tree == null) {
      return null;
    }
    return lowering.lower(tree);
  }

  /**
   * An expression, or null (with the tokens unchanged) if there is none. An array creation, a
   * lambda and a method reference are only accepted as a whole expression (e.g. as an argument).
   */
  public ExprNode parseTree(TokenList tokens) {
    Expression opaque = ctx.arrayInitParser.parse(tokens);
    if (opaque == null) {
      opaque = ctx.lambdaParser.parse(tokens);
    }
    if (opaque != null) {
      return new OpaqueNode(opaque);
    }
    return parseBinary(tokens, 0);
  }

  /**
   * An expression without the special cases of parseTree(), e.g. the value of a compound assignment
   * ("b + c" in "a += b + c"): a cast at its start is that of its first operand.
   */
  public ExprNode parseOperand(TokenList tokens) {
    return operand(tokens, parseBinary(tokens, 0));
  }

  /**
   * The target of an assignment: a name, field or array element, e.g. "a", "a.b" or "a.b[i]" (an
   * index only after the last name, see StatementParser for e.g. "a[i].b" or "f().b"). Returns null
   * (with the tokens unchanged) if there is none.
   */
  public ExprNode parseAssignmentTarget(TokenList tokens) {
    if (!(tokens.curr() instanceof Identifier)) {
      return null;
    }
    int idx = tokens.idx();
    ExprNode target = new NameNode(((Identifier) tokens.curr()).id(), false);
    tokens.next();
    while (tokens.curr().eq(sym(DOT))
        && (tokens.peekNext() instanceof Identifier || tokens.peekNext() instanceof Type)) {
      Token name = tokens.next();
      if (name instanceof Type) {
        target = new SelectNode(target, ((Type) name).name, true);
      } else {
        target = new SelectNode(target, ((Identifier) name).id(), false);
      }
      tokens.next();
    }
    if (tokens.curr().eq(sym(LPAREN))) {
      tokens.setIdx(idx); // a call
      return null;
    }
    while (tokens.curr().eq(sym(LBRAK)) && !tokens.peekNext().eq(sym(RBRAK))) {
      tokens.next();
      ExprNode index = operand(tokens, parseTree(tokens));
      expect(tokens, sym(RBRAK));
      target = new IndexNode(target, index);
    }
    return target;
  }

  /**
   * A primary expression with its selectors, e.g. "a.b.f(x)" or "new Foo().bar()", or null (with
   * the tokens unchanged) if there is none.
   */
  public ExprNode parseSelectorExpression(TokenList tokens) {
    ExprNode primary = parsePrimary(tokens);
    if (primary == null) {
      return null;
    }
    return parseSelectors(tokens, primary);
  }

  /** The tree of a parsed expression (an OpaqueNode for one the tree doesn't model). */
  public static ExprNode tree(Expression expr) {
    if (expr.tree != null) {
      return expr.tree;
    }
    return new OpaqueNode(expr);
  }

  /** The postfix form of an expression tree. */
  public Expression lower(ExprNode tree) {
    return lowering.lower(tree);
  }

  // An expression of operators with at least the given precedence, e.g. "a * b" in "a * b + c"
  // (with a minimum precedence above that of "+").
  private ExprNode parseBinary(TokenList tokens, int minPrecedence) {
    ExprNode left = parseUnary(tokens);
    if (left == null) {
      return null;
    }
    while (true) {
      Token curr = tokens.curr();
      if (curr.eq(sym(QUESTION)) && minPrecedence <= TERNARY) {
        tokens.next();
        ExprNode ifTrue = operand(tokens, parseTree(tokens));
        expect(tokens, sym(COLON));
        ExprNode ifFalse = operand(tokens, parseTree(tokens));
        left = new TernaryNode(left, ifTrue, ifFalse);
      } else if (curr.eq(keyword(INSTANCEOF)) && minPrecedence <= RELATIONAL) {
        tokens.next();
        left = new InstanceOfNode(left, parseInstanceofType(tokens));
      } else if (isBinaryOperator(curr) && ((Operator) curr).precedence() >= minPrecedence) {
        Operator operator = (Operator) curr;
        int precedence = operator.precedence();
        tokens.next();
        // "a = b = c" is "a = (b = c)", "a - b - c" is "(a - b) - c"
        int rightPrecedence = precedence + 1;
        if (precedence == ASSIGN.getPrecedence()) {
          rightPrecedence = precedence;
        }
        ExprNode right = operand(tokens, parseBinary(tokens, rightPrecedence));
        left = new BinaryNode(operator.opValue(), left, right);
      } else {
        return left;
      }
    }
  }

  private static boolean isBinaryOperator(Token token) {
    if (!(token instanceof Operator)) {
      return false;
    }
    Operators op = ((Operator) token).opValue();
    return op != NEGATE
        && op != BITWISE_NOT
        && op != CAST
        && op != PRE_INCREMENT
        && op != PRE_DECREMENT
        && op != POST_INCREMENT
        && op != POST_DECREMENT;
  }

  // e.g. "-x", "!a.b()", "(int) x", "√2" or just "a.b"
  private ExprNode parseUnary(TokenList tokens) {
    Token curr = tokens.curr();
    if (curr instanceof Operator) {
      Operators op = ((Operator) curr).opValue();
      if (op == PLUS) {
        tokens.next();
        return operand(tokens, parseUnary(tokens));
      }
      if (op == MINUS
          || op == NEGATE
          || op == BITWISE_NOT
          || op == PRE_INCREMENT
          || op == PRE_DECREMENT) {
        tokens.next();
        return new UnaryNode(op, operand(tokens, parseUnary(tokens)));
      }
      return null; // e.g. "*"
    }
    if (curr.eq(sym(SQRT))) {
      tokens.next();
      return parseSqrt(tokens);
    }
    if (curr.eq(sym(LPAREN))) {
      String castType = parseCastType(tokens);
      if (castType != null) {
        return new CastNode(castType, operand(tokens, parseUnary(tokens)));
      }
    }
    ExprNode primary = parsePrimary(tokens);
    if (primary == null) {
      return null;
    }
    return parseSelectors(tokens, primary);
  }

  // "√x" is Math.sqrt(x), "√2" is evaluated by the compiler
  private ExprNode parseSqrt(TokenList tokens) {
    Token curr = tokens.curr();
    if (curr instanceof IntLiteral) {
      tokens.next();
      double value = ((IntLiteral) curr).intValue();
      return new LiteralNode(new FPLiteral(Math.sqrt(value), false));
    } else if (curr instanceof FPLiteral) {
      tokens.next();
      double value = ((FPLiteral) curr).doubleVal();
      return new LiteralNode(new FPLiteral(Math.sqrt(value), false));
    }
    ExprNode arg;
    if (curr.eq(sym(LPAREN))) {
      tokens.next();
      arg = operand(tokens, parseTree(tokens));
      expect(tokens, sym(RPAREN));
    } else {
      arg = operand(tokens, parseUnary(tokens));
    }
    List<ExprNode> args = new ArrayList<>();
    args.add(arg);
    return new CallNode(null, "Math.sqrt", args);
  }

  // a literal, a name, a call without a target, "new Foo()", "(expr)", ...
  private ExprNode parsePrimary(TokenList tokens) {
    Token curr = tokens.curr();
    if (curr instanceof IntLiteral
        || curr instanceof FPLiteral
        || curr instanceof BoolLiteral
        || curr instanceof Chr
        || curr instanceof Str) {
      tokens.next();
      return new LiteralNode(curr);
    } else if (curr instanceof Identifier) {
      String name = ((Identifier) curr).id();
      tokens.next();
      if (tokens.curr().eq(sym(LPAREN))) {
        return new CallNode(null, name, parseArguments(tokens));
      }
      return new NameNode(arrayClassName(tokens, name), false);
    } else if (curr instanceof Type) {
      // e.g. "String.valueOf(1)" or "int[].class"
      int idx = tokens.idx();
      tokens.next();
      String name = arrayClassName(tokens, ((Type) curr).name);
      if (!tokens.curr().eq(sym(DOT))) {
        tokens.setIdx(idx);
        return null; // (e.g. a declaration)
      }
      return new NameNode(name, true);
    } else if (curr.eq(sym(LPAREN))) {
      tokens.next();
      ExprNode inner = operand(tokens, parseBinary(tokens, 0));
      expect(tokens, sym(RPAREN));
      return inner;
    } else if (curr.eq(sym(LFLOOR))) {
      // "⌊x⌋" is Math.round(Math.floor(x))
      tokens.next();
      List<ExprNode> args = new ArrayList<>();
      args.add(operand(tokens, parseTree(tokens)));
      expect(tokens, sym(RFLOOR));
      List<ExprNode> floor = new ArrayList<>();
      floor.add(new CallNode(null, "Math.floor", args));
      return new CallNode(null, "Math.round", floor);
    } else if (curr.eq(keyword(NEW))) {
      return parseNew(tokens);
    }
    return null;
  }

  // the name of an array class literal, e.g. "int[]" in "int[].class" (otherwise the given name)
  private static String arrayClassName(TokenList tokens, String name) {
    int idx = tokens.idx();
    String arrayName = name;
    while (tokens.curr().eq(sym(LBRAK)) && tokens.peekNext().eq(sym(RBRAK))) {
      arrayName += "[]";
      tokens.next();
      tokens.next();
    }
    if (arrayName.length() > name.length()
        && (!tokens.curr().eq(sym(DOT)) || !tokens.peekNext().eq(keyword(CLASS)))) {
      tokens.setIdx(idx);
      return name;
    }
    return arrayName;
  }

  // "new Foo(x)", "new java.util.HashMap<>()" (the type arguments are erased)
  private ExprNode parseNew(TokenList tokens) {
    Token typeToken = tokens.next();
    String type;
    if (typeToken instanceof Identifier) {
      type = qualifiedName(tokens);
    } else if (typeToken instanceof Type) {
      type = ((Type) typeToken).name;
      tokens.next();
    } else {
      ctx.errors.addError(
          LOG_NAME,
          "identifier or type expected after \"new\"",
          new RuntimeException().getStackTrace());
      return new OpaqueNode(new Expression());
    }
    int end = typeArgumentsEnd(tokens, tokens.idx());
    if (end > 0) {
      tokens.setIdx(end);
    }
    if (!tokens.curr().eq(sym(LPAREN))) {
      ctx.errors.missingExpected(
          LOG_NAME, sym(LPAREN), tokens, new RuntimeException().getStackTrace());
      return new OpaqueNode(new Expression());
    }
    NewNode creation = new NewNode(type, parseArguments(tokens));
    if (tokens.curr().eq(sym(LBRACE))) {
      // an anonymous class
      tokens.next();
      creation.body = ctx.classParser.parseClassBody(tokens, false);
      expect(tokens, sym(RBRACE));
    }
    return creation;
  }

  // A (possibly qualified) name, e.g. "a" or "java.lang.String" (the current token is its first
  // identifier).
  private static String qualifiedName(TokenList tokens) {
    StringBuilder name = new StringBuilder(((Identifier) tokens.curr()).id());
    tokens.next();
    while (tokens.curr().eq(sym(DOT))
        && (tokens.peekNext() instanceof Identifier || tokens.peekNext() instanceof Type)) {
      Token segment = tokens.next();
      if (segment instanceof Type) {
        name.append('.').append(((Type) segment).name);
      } else {
        name.append('.').append(((Identifier) segment).id());
      }
      tokens.next();
    }
    return name.toString();
  }

  // If the token at the given index starts type arguments (e.g. "<String, List<T>>"), returns the
  // index after them, otherwise -1.
  private static int typeArgumentsEnd(TokenList tokens, int start) {
    if (!tokens.get(start).eq(op(LESS))) {
      return -1;
    }
    int depth = 1;
    int i = start;
    while (depth > 0) {
      i++;
      Token t = tokens.get(i);
      if (t.eq(op(LESS))) {
        depth++;
      } else if (t.eq(op(GREATER))) {
        depth--;
      } else if (t.eq(op(RSHIFT))) {
        depth -= 2;
      } else if (t.eq(op(RSHIFTU))) {
        depth -= 3;
      } else if (!(t instanceof Identifier)
          && !(t instanceof Type)
          && !t.eq(sym(DOT))
          && !t.eq(sym(COMMA))
          && !t.eq(sym(QUESTION))
          && !t.eq(sym(LBRAK))
          && !t.eq(sym(RBRAK))
          && !(t instanceof Keyword)) {
        return -1; // (also at the end of the input)
      }
    }
    return (depth == 0) ? i + 1 : -1;
  }

  // "(a, b)", the arguments of a call (the current token is the "(")
  private List<ExprNode> parseArguments(TokenList tokens) {
    List<ExprNode> args = new ArrayList<>();
    tokens.next();
    ExprNode arg = parseTree(tokens);
    if (arg != null) {
      args.add(arg);
      while (tokens.curr().eq(sym(COMMA))) {
        tokens.next();
        args.add(operand(tokens, parseTree(tokens)));
      }
    }
    expect(tokens, sym(RPAREN));
    return args;
  }

  // the selectors and postfix operators after a primary expression, e.g. ".b", ".f(x)", "[i]",
  // "++"
  private ExprNode parseSelectors(TokenList tokens, ExprNode primary) {
    ExprNode node = primary;
    while (true) {
      Token curr = tokens.curr();
      Token next = tokens.peekNext();
      if (curr.eq(sym(DOT)) && next.eq(keyword(NEW))) {
        // a new instance of an inner class of an object, e.g. "outer.new Inner()"
        tokens.next();
        ExprNode creation = parseNew(tokens);
        if (creation instanceof NewNode) {
          ((NewNode) creation).outer = node;
        }
        node = creation;
      } else if (curr.eq(sym(DOT)) && next.eq(op(LESS))) {
        // explicit type arguments of a call, e.g. "Foo.<String>bar()" (ignored)
        int idx = tokens.idx();
        tokens.next();
        int errorCount = ctx.errors.errors.size();
        if (ctx.typeArgsParser.parse(tokens) == null
            || ctx.errors.errors.size() > errorCount
            || !(tokens.curr() instanceof Identifier)
            || !tokens.peekNext().eq(sym(LPAREN))) {
          tokens.setIdx(idx);
          return node;
        }
        String name = ((Identifier) tokens.curr()).id();
        tokens.next();
        node = new CallNode(node, name, parseArguments(tokens));
      } else if (curr.eq(sym(DOT)) && next instanceof Identifier) {
        tokens.next();
        tokens.next();
        String name = ((Identifier) next).id();
        if (tokens.curr().eq(sym(LPAREN))) {
          node = new CallNode(node, name, parseArguments(tokens));
        } else {
          node = new SelectNode(node, name, false);
        }
      } else if (curr.eq(sym(DOT)) && next instanceof Type) {
        // a qualified name, e.g. "java.lang.String"
        tokens.next();
        tokens.next();
        node = new SelectNode(node, ((Type) next).name, true);
      } else if (curr.eq(sym(DOT)) && next.eq(keyword(CLASS))) {
        tokens.next();
        tokens.next();
        node = new SelectNode(node, "class", false);
      } else if (curr.eq(sym(LBRAK)) && !next.eq(sym(RBRAK))) {
        tokens.next();
        ExprNode index = operand(tokens, parseTree(tokens));
        expect(tokens, sym(RBRAK));
        node = new IndexNode(node, index);
      } else if (curr.eq(op(POST_INCREMENT)) || curr.eq(op(POST_DECREMENT))) {
        tokens.next();
        node = new UnaryNode(((Operator) curr).opValue(), node);
      } else {
        return node;
      }
    }
  }

  // the type of "x instanceof T" (not resolved), e.g. "String" or "java.util.List"
  private String parseInstanceofType(TokenList tokens) {
    Token curr = tokens.curr();
    if (curr instanceof Identifier) {
      return qualifiedName(tokens);
    } else if (curr instanceof Type) {
      tokens.next();
      return ((Type) curr).name;
    }
    ctx.errors.addError(
        LOG_NAME,
        "expected a type but found " + curr.display(),
        new RuntimeException().getStackTrace());
    return "java.lang.Object";
  }

  // If the tokens start with a cast, e.g. "(int)", "(String)" or "(java.util.List<T>)" followed by
  // its operand, consumes the parentheses and returns the (resolved) type, otherwise returns null
  // (with the tokens unchanged). The current token is the "(".
  private String parseCastType(TokenList tokens) {
    int idx = tokens.idx();
    Token first = tokens.next();
    String type = null;
    if (first instanceof Type) {
      type = ((Type) first).name;
      tokens.next();
    } else if (first instanceof Identifier) {
      String name = qualifiedName(tokens);
      // (a class name starts with an upper-case letter, e.g. not "(a)")
      if (Character.isUpperCase(name.charAt(name.lastIndexOf('.') + 1))) {
        type = name;
      }
      int end = typeArgumentsEnd(tokens, tokens.idx());
      if (end > 0) {
        tokens.setIdx(end); // erased
      }
    }
    // (an array type, e.g. "(int[]) o")
    int dims = 0;
    while (type != null && tokens.curr().eq(sym(LBRAK)) && tokens.peekNext().eq(sym(RBRAK))) {
      tokens.next();
      tokens.next();
      dims++;
    }
    if (type != null && tokens.curr().eq(sym(RPAREN))) {
      tokens.next();
      boolean primitive = dims == 0 && TypeUtils.isPrimitive(type);
      if (startsCastOperand(tokens, primitive)) {
        if (primitive) {
          return type;
        } else if (dims > 0) {
          String element = TypeUtils.isPrimitive(type) ? type : resolveCastType(type);
          return TypeUtils.descr(element, dims);
        }
        return resolveCastType(type);
      }
    }
    tokens.setIdx(idx);
    return null;
  }

  // Whether the current token can start the operand of a cast. A "+" or "-" only after a cast to a
  // primitive type: "(a) - b" is a subtraction.
  private static boolean startsCastOperand(TokenList tokens, boolean primitive) {
    Token curr = tokens.curr();
    if (curr instanceof Operator) {
      Operators op = ((Operator) curr).opValue();
      if (op == NEGATE || op == BITWISE_NOT) {
        return true;
      }
      return primitive && (op == MINUS || op == PLUS || op == PRE_INCREMENT || op == PRE_DECREMENT);
    }
    return curr instanceof Identifier
        || curr instanceof Type
        || curr instanceof IntLiteral
        || curr instanceof FPLiteral
        || curr instanceof BoolLiteral
        || curr instanceof Chr
        || curr instanceof Str
        || curr.eq(sym(LPAREN))
        || curr.eq(sym(SQRT))
        || curr.eq(sym(LFLOOR))
        || curr.eq(keyword(NEW));
  }

  private String resolveCastType(String name) {
    if (name.contains(".")) {
      return ctx.typeArgsParser.resolveQualified(name);
    }
    String fqn = ctx.importMap.get(name);
    if (fqn != null) {
      return fqn;
    }
    if (ctx.currClazz != null && name.equals(ctx.currClazz.name)) {
      return ctx.currClazz.fqn();
    }
    try {
      Class.forName("java.lang." + name);
      return "java.lang." + name;
    } catch (ClassNotFoundException e) {
      // not in java.lang
    }
    if (ctx.currClazz != null
        && ctx.currClazz.packageName != null
        && !ctx.currClazz.packageName.isEmpty()) {
      return ctx.currClazz.packageName + "." + name;
    }
    return name;
  }

  // Reports a missing operand, e.g. in "1 +;", and returns a placeholder for it.
  private ExprNode operand(TokenList tokens, ExprNode node) {
    if (node != null) {
      return node;
    }
    ctx.errors.addError(
        LOG_NAME,
        "expected an expression but found " + tokens.curr().display(),
        new RuntimeException().getStackTrace());
    return new OpaqueNode(new Expression());
  }

  private void expect(TokenList tokens, Token expected) {
    if (tokens.curr().eq(expected)) {
      tokens.next();
    } else {
      ctx.errors.missingExpected(
          LOG_NAME, expected, tokens, new RuntimeException().getStackTrace());
    }
  }
}
