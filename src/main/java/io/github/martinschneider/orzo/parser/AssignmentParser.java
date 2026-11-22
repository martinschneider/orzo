package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.tree.BinaryNode;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import java.util.ArrayList;
import java.util.List;

public class AssignmentParser implements ProdParser<Assignment> {
  private static final String LOG_NAME = "parse assignment";
  private ParserContext ctx;

  public AssignmentParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Assignment parse(TokenList tokens) {
    List<Identifier> left = new ArrayList<>();
    List<Expression> right = new ArrayList<>();
    int idx = tokens.idx();
    // not a declaration, e.g. "Argument other = ..." or "io.pkg.Argument other = ..."
    if (tokens.curr() instanceof Identifier) {
      int scanIdx = tokens.idx();
      while (tokens.peekNext().eq(sym(DOT))) {
        tokens.next(); // move to DOT
        // The last segment of a built-in-sounding FQN (e.g. "java.lang.String") lexes as a
        // Type token, not an Identifier — Type.BASIC_TYPES includes "String" alongside the
        // primitives.
        if (!(tokens.peekNext() instanceof Identifier) && !(tokens.peekNext() instanceof Type)) {
          break;
        }
        tokens.next(); // move to the next segment
      }
      String lastSegment = tokens.curr().toString();
      // "Type name", "Type<Args> name" (a statement can't start with a "<" comparison) or
      // "Type[] name" (an empty "[]" can't be an array access).
      boolean looksLikeDeclaration =
          !lastSegment.isEmpty()
              && Character.isUpperCase(lastSegment.charAt(0))
              && (tokens.peekNext() instanceof Identifier
                  || tokens.peekNext().eq(op(LESS))
                  || (tokens.peekNext().eq(sym(LBRAK))
                      && tokens.get(tokens.idx() + 2).eq(sym(RBRAK))));
      tokens.setIdx(scanIdx);
      if (looksLikeDeclaration) {
        return null;
      }
    }
    // the targets, e.g. "a" and "b.c[i]" in "a, b.c[i] = 1, 2;"
    List<ExprNode> targets = new ArrayList<>();
    while (tokens.curr() instanceof Identifier) {
      ExprNode target = ctx.exprParser.parseAssignmentTarget(tokens);
      if (target == null) {
        break;
      }
      targets.add(target);
      left.add((Identifier) ctx.exprParser.lower(target).tokens.get(0));
      if (!tokens.curr().eq(sym(COMMA))) {
        break;
      }
      tokens.next();
    }
    if (targets.isEmpty() || !(tokens.curr() instanceof Operator)) {
      tokens.setIdx(idx);
      return null;
    }
    Operators op = ((Operator) tokens.curr()).opValue();
    if (op != ASSIGN) {
      // "a += b" is "a = a + b"
      Operators arithmeticOp = arithmeticOperator(op);
      if (arithmeticOp == null) {
        // not an assignment (e.g. "x++;"): back to where we started
        tokens.setIdx(idx);
        return null;
      }
      tokens.next();
      ExprNode value = ctx.exprParser.parseOperand(tokens);
      right.add(ctx.exprParser.lower(new BinaryNode(arithmeticOp, targets.get(0), value)));
    } else {
      tokens.next();
      // (parallel assignments: values separated by commas, e.g. "a, b = b, a;")
      Expression expression = null;
      while ((expression = ctx.exprParser.parse(tokens)) != null) {
        right.add(expression);
        if (!tokens.curr().eq(sym(COMMA))) {
          break;
        }
        tokens.next();
      }
      if (right.isEmpty()) {
        ctx.errors.addError(
            LOG_NAME,
            "expected an expression but found " + tokens.curr().display(),
            new RuntimeException().getStackTrace());
      }
    }
    Assignment assignment = new Assignment(left, right);
    if (tokens.curr().eq(sym(SEMICOLON))) {
      tokens.next();
    }
    return assignment;
  }

  // the operator of a compound assignment, e.g. PLUS for "+=" (null for any other operator)
  private static Operators arithmeticOperator(Operators op) {
    if (op == PLUS_ASSIGN) {
      return PLUS;
    } else if (op == MINUS_ASSIGN) {
      return MINUS;
    } else if (op == TIMES_ASSIGN) {
      return TIMES;
    } else if (op == DIV_ASSIGN) {
      return DIV;
    } else if (op == MOD_ASSIGN) {
      return MOD;
    } else if (op == LSHIFT_ASSIGN) {
      return LSHIFT;
    } else if (op == RSHIFT_ASSIGN) {
      return RSHIFT;
    } else if (op == RSHIFTU_ASSIGN) {
      return RSHIFTU;
    } else if (op == BITWISE_AND_ASSIGN) {
      return BITWISE_AND;
    } else if (op == BITWISE_OR_ASSIGN) {
      return BITWISE_OR;
    } else if (op == BITWISE_XOR_ASSIGN) {
      return BITWISE_XOR;
    }
    return null;
  }
}
