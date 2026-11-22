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
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;
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
    // Non-destructive lookahead: if this looks like "Type.Name varName" (a declaration, e.g.
    // "Argument other = ..." or "io.pkg.Argument other = ..."), bail out before the dotted-chain
    // loop below, which *removes* DOT/Identifier tokens as it merges them into a Identifier.next
    // chain — if that mutation runs and we only then discover this isn't an assignment, the
    // removed tokens are gone for good even after resetting tokens.idx() back to `idx`.
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
    while (tokens.curr() instanceof Identifier || tokens.curr().eq(sym(COMMA))) {
      if (tokens.curr() instanceof Identifier) {
        Identifier id = (Identifier) tokens.curr();
        Identifier root = id;
        while (tokens.peekNext().eq(sym(DOT))) {
          tokens.next();
          if (tokens.peekNext() instanceof Identifier) {
            Identifier next = (Identifier) tokens.next();
            id.next = next;
            id = id.next;
            tokens.remove(tokens.idx());
            tokens.remove(tokens.idx() - 1);
            tokens.prev();
            tokens.prev();
          }
        }
        tokens.next();
        // the selector belongs to the last link, e.g. "a.b[i] = x"
        ArraySelector arrSel = ctx.arraySelectorParser.parse(tokens);
        if (arrSel != null) {
          id.arrSel = arrSel;
        }
        id = root;
        left.add(id);
      } else {
        tokens.next();
      }
    }
    // If the first identifier looks like a type name (starts with uppercase) followed by a variable
    // name, this is a declaration (e.g. "Argument other = ..."), not an assignment.
    if (left.size() == 2
        && left.get(0).next == null
        && left.get(0).arrSel == null
        && !left.get(0).val.toString().isEmpty()
        && Character.isUpperCase(left.get(0).val.toString().charAt(0))) {
      tokens.setIdx(idx);
      return null;
    }
    if (tokens.curr() instanceof Operator) {
      if (tokens.curr().eq(op(ASSIGN))) {
        tokens.next();
      } else if (tokens.curr().eq(op(POST_INCREMENT)) || tokens.curr().eq(op(POST_DECREMENT))) {
        // see post increment parser
        tokens.setIdx(idx);
        return null;
      } else if (tokens.curr().eq(op(PLUS_ASSIGN))) {
        rewriteCompoundAssign(tokens, PLUS, left.get(0));
      } else if (tokens.curr().eq(op(MINUS_ASSIGN))) {
        rewriteCompoundAssign(tokens, MINUS, left.get(0));
      } else if (tokens.curr().eq(op(TIMES_ASSIGN))) {
        rewriteCompoundAssign(tokens, TIMES, left.get(0));
      } else if (tokens.curr().eq(op(DIV_ASSIGN))) {
        rewriteCompoundAssign(tokens, DIV, left.get(0));
      } else if (tokens.curr().eq(op(MOD_ASSIGN))) {
        rewriteCompoundAssign(tokens, MOD, left.get(0));
      } else if (tokens.curr().eq(op(LSHIFT_ASSIGN))) {
        rewriteCompoundAssign(tokens, LSHIFT, left.get(0));
      } else if (tokens.curr().eq(op(RSHIFT_ASSIGN))) {
        rewriteCompoundAssign(tokens, RSHIFT, left.get(0));
      } else if (tokens.curr().eq(op(RSHIFTU_ASSIGN))) {
        rewriteCompoundAssign(tokens, RSHIFTU, left.get(0));
      } else if (tokens.curr().eq(op(BITWISE_AND_ASSIGN))) {
        rewriteCompoundAssign(tokens, BITWISE_AND, left.get(0));
      } else if (tokens.curr().eq(op(BITWISE_OR_ASSIGN))) {
        rewriteCompoundAssign(tokens, BITWISE_OR, left.get(0));
      } else if (tokens.curr().eq(op(BITWISE_XOR_ASSIGN))) {
        rewriteCompoundAssign(tokens, BITWISE_XOR, left.get(0));
      } else {
        // not an assignment (e.g. "++x;"): back to where we started
        tokens.setIdx(idx);
        return null;
      }
      Expression expression = null;
      while ((expression = ctx.exprParser.parse(tokens)) != null || tokens.curr().eq(sym(COMMA))) {
        if (expression != null) {
          right.add(expression);
        } else {
          tokens.next();
        }
      }
      if (right.isEmpty()) {
        ctx.errors.addError(
            LOG_NAME,
            "expected an expression but found " + tokens.curr().display(),
            new RuntimeException().getStackTrace());
      }
      Assignment assignment = new Assignment(left, right);
      if (tokens.curr().eq(sym(SEMICOLON))) {
        tokens.next();
      }
      return assignment;
    }
    tokens.setIdx(idx);
    return null;
  }

  // Rewrites "lhs OP= rhs" to "lhs = lhs OP (rhs)" at the raw token level. The parenthesis
  // around rhs is required: without it, a lower-precedence construct in rhs (e.g. a trailing
  // ternary "? :") would swallow the inserted "lhs OP" into its own condition/branch instead of
  // being applied to the ternary's result, since the rewritten tokens are fed back into the same
  // expression parser with no memory of where the original rhs started.
  private void rewriteCompoundAssign(TokenList tokens, Operators arithOp, Identifier left) {
    tokens.insert(sym(LPAREN));
    tokens.insert(op(arithOp));
    tokens.insert(left);
    tokens.insert(op(ASSIGN));
    tokens.next();
    tokens.next(); // the right-hand side starts after the inserted "="
    // rhs ends at the statement's top-level SEMICOLON, or — for a for-loop increment clause,
    // which has no trailing semicolon (e.g. "for (...; ...; i += 2)") — at the first RPAREN that
    // isn't balanced by one of rhs's own LPARENs (the loop header's closing paren). Start the
    // scan past our own synthetic LPAREN (the tokens from the current one on are, in order:
    // left, arithOp, LPAREN) — depth==0 means "no paren opened by the original rhs is still open",
    // not counting our own wrapper paren.
    int depth = 0;
    int rhsEnd = tokens.idx() + 3;
    while (rhsEnd < tokens.list().size()) {
      Token curr = tokens.get(rhsEnd);
      if (curr.eq(sym(LPAREN))) {
        depth++;
      } else if (curr.eq(sym(RPAREN))) {
        if (depth == 0) {
          break;
        }
        depth--;
      } else if (depth == 0 && curr.eq(sym(SEMICOLON))) {
        break;
      }
      rhsEnd++;
    }
    tokens.insert(rhsEnd, sym(RPAREN));
  }
}
