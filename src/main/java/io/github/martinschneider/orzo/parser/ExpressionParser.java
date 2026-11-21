package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.QUESTION;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SQRT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.eof;
import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.InstanceofToken;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Keywords;
import io.github.martinschneider.orzo.lexer.tokens.Num;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.TernaryExpression;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ExpressionParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse expression";

  public ExpressionParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  // TODO: simplify and document this
  public Expression parse(TokenList tokens) {
    Expression expr = null;
    if ((expr = ctx.arrayInitParser.parse(tokens)) != null) {
      return expr;
    }
    if ((expr = ctx.lambdaParser.parse(tokens)) != null) {
      return expr;
    }
    Type cast = ctx.castParser.parse(tokens);
    List<Token> exprTokens = new ArrayList<>();
    // Number of synthetic ')' tokens (closing a "(-1) * (" rewrite of a unary minus applied to a
    // parenthesized sub-expression, e.g. "-(a+b)") still owed once the real parenthesis depth
    // returns to 0.
    int[] pendingNegatedParens = {0};
    checkNegative(tokens, exprTokens, pendingNegatedParens);
    int parenthesis = 0;
    // Track the index in exprTokens where the last depth-0->(-1) LPAREN was added,
    // and the position in exprTokens just after that LPAREN (where the condition starts).
    // indices in exprTokens of the currently open '(' tokens, innermost first: a '?' inside
    // parentheses makes the innermost one the opening paren of a ternary
    java.util.Deque<Integer> openParens = new java.util.ArrayDeque<>();
    // Re-entry loop: a resolved "((Type) expr).field"/"((Type) expr).method()" chain replaces
    // exprTokens with a single CastMethodCall token (below) and leaves `tokens` positioned right
    // after it - which may be mid-expression (e.g. "== 5" still unconsumed in
    // "((Type) expr).field == 5"), not necessarily at the expression's end. The inner while loop
    // doesn't accept a DOT to continue on (DOT-chains are only handled per-token-group inside
    // it), so without this, that trailing operator+operand was silently dropped - the caller got
    // back an incomplete expression and whatever came next (e.g. IfParser expecting ')') choked
    // on the leftover tokens. Re-entering with the existing exprTokens/cast/parenthesis state
    // lets the SAME unchanged loop body keep consuming normally (EQUAL/Num/etc. are already
    // accepted token kinds) - a no-op for every expression that never hits the cast-chain case.
    boolean continueParsing = true;
    while (continueParsing) {
      continueParsing = false;
      outer:
      {
        while (tokens.curr() instanceof Num
            || tokens.curr() instanceof BoolLiteral
            || tokens.curr() instanceof Str
            || tokens.curr() instanceof Chr
            || tokens.curr() instanceof Identifier
            || (tokens.curr() instanceof Type && parenthesis < 0)
            // Dotted static access on a built-in type name (e.g. "String.valueOf(x)",
            // "String.class") - previously only accepted as the very FIRST thing in the
            // expression ("exprTokens.isEmpty()"), an intentionally narrow scope from when this
            // was added (see commit history, "Add Type.class literal support"). That meant
            // "String.valueOf(x)" as a non-first operand (e.g. "a + String.valueOf(x)") wasn't
            // an accepted continuation token at all, so the outer loop exited early mid-
            // expression, silently dropping everything from there on for the caller.
            || (tokens.curr() instanceof Type && parenthesis == 0 && tokens.peekNext().eq(sym(DOT)))
            || tokens.curr() instanceof Operator
            || tokens.curr().eq(sym(SQRT))
            || tokens.curr().eq(sym(LPAREN))
            || tokens.curr().eq(sym(RPAREN))
            || tokens.curr().eq(sym(QUESTION))
            || isNewKeyword(tokens.curr())
            || isInstanceofKeyword(tokens.curr())) {
          if (tokens.curr().eq(sym(LPAREN))) {
            stripGenericCastArgs(tokens);
          }
          // Handle ternary: when we see '?' at depth -1, parse it as a ternary expression
          if (tokens.curr().eq(sym(QUESTION)) && parenthesis < 0 && !openParens.isEmpty()) {
            int ternaryOuterParenIdx = openParens.pop();
            // conditionTokens = exprTokens from ternaryOuterParenIdx+1 to end (exclude the '(')
            List<Token> condTokens =
                new ArrayList<>(exprTokens.subList(ternaryOuterParenIdx + 1, exprTokens.size()));
            // prefix = exprTokens[0..ternaryOuterParenIdx-1] (everything before the '(')
            List<Token> prefixTokens = new ArrayList<>(exprTokens.subList(0, ternaryOuterParenIdx));
            tokens.next(); // consume '?'
            Expression condition = new Expression(postfix(condTokens), null);
            // Parse true branch (until ':') — parse() stops naturally at ':' since it's not in the
            // loop's accepted-token set
            Expression trueBranch = parse(tokens);
            // consume ':'
            if (tokens.curr().eq(sym(COLON))) {
              tokens.next();
            }
            // Parse false branch (until ')' that closes the ternary's outer '(')
            Expression falseBranch = parse(tokens);
            // consume the closing ')' of the ternary
            if (tokens.curr().eq(sym(RPAREN))) {
              tokens.next();
            }
            TernaryExpression ternary = new TernaryExpression(condition, trueBranch, falseBranch);
            // Reset: prefix + ternary as a single token
            exprTokens = prefixTokens;
            exprTokens.add(ternary);
            parenthesis++; // its closing ')' was consumed above
            // Continue collecting the rest of the outer expression
            continue;
          } else if (tokens.curr().eq(sym(QUESTION)) && parenthesis == 0 && !exprTokens.isEmpty()) {
            // Ternary at depth 0: all collected tokens form the condition
            List<Token> condTokens = new ArrayList<>(exprTokens);
            tokens.next(); // consume '?'
            Expression condition = new Expression(postfix(condTokens), null);
            Expression trueBranch = parse(tokens);
            if (tokens.curr().eq(sym(COLON))) {
              tokens.next();
            }
            Expression falseBranch = parse(tokens);
            TernaryExpression ternary = new TernaryExpression(condition, trueBranch, falseBranch);
            exprTokens = new ArrayList<>();
            exprTokens.add(ternary);
            continue;
          }

          if (isInstanceofKeyword(tokens.curr())) {
            tokens.next(); // consume 'instanceof'
            String typeName = tokens.curr().val.toString();
            tokens.next(); // consume type name
            // fully-qualified type, e.g. "x instanceof java.lang.String"
            while (tokens.curr().eq(sym(DOT))
                && (tokens.peekNext() instanceof Identifier || tokens.peekNext() instanceof Type)) {
              Token seg = tokens.next();
              typeName += "." + (seg instanceof Type ? ((Type) seg).name : seg.val.toString());
              tokens.next();
            }
            exprTokens.add(new InstanceofToken(typeName));
            continue;
          }
          int idx = tokens.idx();
          boolean negative = false;
          Token prevToken = tokens.peekPrev();
          if (prevToken.eq(sym(LPAREN))
              || (prevToken instanceof Operator
                  && ((Operator) prevToken).opValue().getPrecedence() <= 12)) {
            negative = checkNegative(tokens, exprTokens, pendingNegatedParens);
          }
          if (negative) {
            idx = tokens.idx();
          }
          List<Identifier> selectors = new ArrayList<>();
          boolean strReceiver = false;
          do {
            idx = tokens.idx();
            MethodCall methodCall = parseMethod(tokens);
            if (methodCall != null) {
              selectors.add(methodCall);
            } else {
              tokens.setIdx(idx);
              if (tokens.curr().eq(sym(LPAREN))) {
                // Record where this potential ternary-opening '(' goes in exprTokens
                openParens.push(exprTokens.size());
                parenthesis--;
              } else if (tokens.curr().eq(sym(RPAREN))) {
                parenthesis++;
                if (!openParens.isEmpty()) {
                  openParens.pop(); // closed without a '?' → not a ternary
                }
              }
              if (parenthesis > 0) {
                break outer;
              }
              Token curr = tokens.curr();
              ConstructorCall constrCall = null;
              if (!curr.eq(eof())) {
                if (curr instanceof Identifier) {
                  selectors.add((Identifier) curr);
                  tokens.next();
                } else if (!selectors.isEmpty() && curr instanceof Type) {
                  // last segment of a qualified name, e.g. "String" in "java.lang.String"
                  selectors.add(new Identifier(((Type) curr).name));
                  tokens.next();
                } else if (selectors.isEmpty()
                    && curr instanceof Type
                    && tokens.peekNext().eq(sym(DOT))) {
                  // Dotted static access on a built-in type name, e.g. String.class, String.valueOf
                  selectors.add(new Identifier(((Type) curr).name));
                  tokens.next();
                } else if (curr instanceof Keyword
                    && ((Keyword) curr).eq(keyword(Keywords.CLASS))) {
                  // Class literal selector, e.g. String.class
                  selectors.add(new Identifier("class"));
                  tokens.next();
                } else if ((constrCall = ctx.constrCallParser.parse(tokens, true)) != null) {
                  selectors.add(constrCall);
                } else {
                  exprTokens.add(curr);
                  tokens.next();
                  if (curr.eq(sym(RPAREN)) && parenthesis == 0 && pendingNegatedParens[0] > 0) {
                    for (int k = 0; k < pendingNegatedParens[0]; k++) {
                      exprTokens.add(sym(RPAREN));
                    }
                    pendingNegatedParens[0] = 0;
                  }
                  // For Str literals: allow DOT-chained method calls (e.g. "string".method(arg))
                  if (!(curr instanceof Str)) {
                    break;
                  }
                  strReceiver = true;
                }
              }
            }
          } while (tokens.curr().eq(sym(DOT)) && tokens.next() != null);
          if (!selectors.isEmpty()) {
            Token flat = flattenId(selectors);
            if (strReceiver && flat instanceof MethodCall) {
              ((MethodCall) flat).literalReceiver = true;
            }
            exprTokens.add(flat);
          }
        }
      }
      // Handle ((Type) inner) — balanced parenthesized cast expression without following DOT
      if (parenthesis == 0 && !tokens.curr().eq(sym(DOT)) && isCastMethodChainPattern(exprTokens)) {
        cast = new Type(resolveCastType(castTypeName(exprTokens.get(2))));
        exprTokens = new ArrayList<>(exprTokens.subList(4, exprTokens.size() - 1));
      }
      // Handle ((Type) inner).method() — DOT after balanced parenthesized cast expression
      // (also as a later operand, e.g. "a && ((Type) expr).method()")
      int castStart = tokens.curr().eq(sym(DOT)) ? castChainStart(exprTokens) : -1;
      if (castStart >= 0) {
        String castType = resolveCastType(castTypeName(exprTokens.get(castStart + 2)));
        List<Token> innerTokens =
            new ArrayList<>(exprTokens.subList(castStart + 4, exprTokens.size() - 1));
        Expression innerExpr = new Expression(postfix(innerTokens), null);
        exprTokens = new ArrayList<>(exprTokens.subList(0, castStart));
        openParens.removeIf(parenIdx -> parenIdx >= castStart);
        tokens.next(); // skip DOT
        List<Identifier> chainSelectors = new ArrayList<>();
        do {
          int chainIdx = tokens.idx();
          MethodCall mc = parseMethod(tokens);
          if (mc != null) {
            chainSelectors.add(mc);
          } else {
            tokens.setIdx(chainIdx);
            if (tokens.curr() instanceof Identifier && !tokens.curr().eq(eof())) {
              chainSelectors.add((Identifier) tokens.curr());
              tokens.next();
            } else {
              break;
            }
          }
        } while (tokens.curr().eq(sym(DOT)) && tokens.next() != null);
        CastMethodCall castCall = new CastMethodCall(castType, innerExpr);
        if (!chainSelectors.isEmpty()) {
          castCall.next = (Identifier) flattenId(chainSelectors);
        }
        exprTokens.add(castCall);
        // tokens.curr() may now be a binary operator continuing the SAME expression (e.g. "== 5"
        // in "((Type) expr).field == 5") - re-enter the loop above so it keeps consuming instead of
        // this function returning early with the trailing operator+operand left unconsumed.
        continueParsing = true;
      }
    }
    return (exprTokens.size() > 0) ? new Expression(postfix(exprTokens), cast) : null;
  }

  // "(Type<Args>)" can only be a cast: drop the (erased) type arguments from the token stream, so
  // the cast patterns below don't need to know about them (and a "?" wildcard isn't mistaken for
  // a ternary). Expects the current token to be "(".
  private void stripGenericCastArgs(TokenList tokens) {
    int i = tokens.idx() + 1;
    if (!(tokens.get(i) instanceof Identifier) && !(tokens.get(i) instanceof Type)) {
      return;
    }
    i++;
    while (tokens.get(i).eq(sym(DOT))
        && (tokens.get(i + 1) instanceof Identifier || tokens.get(i + 1) instanceof Type)) {
      i += 2;
    }
    if (!tokens.get(i).eq(op(LESS))) {
      return;
    }
    int start = i;
    int depth = 1;
    while (depth > 0) {
      i++;
      Token t = tokens.get(i);
      if (t.eq(eof())) {
        return;
      } else if (t.eq(op(LESS))) {
        depth++;
      } else if (t.eq(op(GREATER))) {
        depth--;
      } else if (t.eq(op(RSHIFT))) {
        depth -= 2;
      } else if (t.eq(op(RSHIFTU))) {
        depth -= 3;
      }
    }
    if (depth < 0 || !tokens.get(i + 1).eq(sym(RPAREN))) {
      return;
    }
    for (int k = i; k >= start; k--) {
      tokens.remove(k);
    }
  }

  // If exprTokens ends with a parenthesized cast expression "((Type) inner)", returns the index of
  // its opening parenthesis, otherwise -1.
  private int castChainStart(List<Token> exprTokens) {
    int last = exprTokens.size() - 1;
    if (last < 0 || !exprTokens.get(last).eq(sym(RPAREN))) {
      return -1;
    }
    int depth = 0;
    int start = last;
    for (; start >= 0; start--) {
      Token t = exprTokens.get(start);
      if (t.eq(sym(RPAREN))) {
        depth++;
      } else if (t.eq(sym(LPAREN))) {
        depth--;
        if (depth == 0) {
          break;
        }
      }
    }
    if (start < 0 || last - start < 5 || !exprTokens.get(start + 1).eq(sym(LPAREN))) {
      return -1;
    }
    Token type = exprTokens.get(start + 2);
    if (!(type instanceof Identifier) && !(type instanceof Type)) {
      return -1;
    }
    String name = castTypeName(type);
    name = name.substring(name.lastIndexOf('.') + 1); // e.g. java.util.List → List
    if (name.isEmpty()
        || !Character.isUpperCase(name.charAt(0))
        || !exprTokens.get(start + 3).eq(sym(RPAREN))) {
      return -1;
    }
    return start;
  }

  private boolean isCastMethodChainPattern(List<Token> exprTokens) {
    if (exprTokens.size() < 5) return false;
    if (!exprTokens.get(0).eq(sym(LPAREN))) return false;
    if (!exprTokens.get(1).eq(sym(LPAREN))) return false;
    Token third = exprTokens.get(2);
    if (!(third instanceof Identifier) && !(third instanceof Type)) return false;
    String name = castTypeName(third);
    name = name.substring(name.lastIndexOf('.') + 1);
    if (name.isEmpty() || !Character.isUpperCase(name.charAt(0))) return false;
    if (!exprTokens.get(3).eq(sym(RPAREN))) return false;
    if (!exprTokens.get(exprTokens.size() - 1).eq(sym(RPAREN))) return false;
    return true;
  }

  // The (possibly dotted) type name of a cast, e.g. "java.lang.reflect.Type" for the Identifier
  // chain java -> lang -> reflect -> Type
  private static String castTypeName(Token token) {
    StringBuilder name = new StringBuilder(token.val.toString());
    if (token instanceof Identifier) {
      for (Identifier link = ((Identifier) token).next; link != null; link = link.next) {
        name.append('.').append(link.val.toString());
      }
    }
    return name.toString();
  }

  private String resolveCastType(String name) {
    if (name.contains(".")) {
      return ctx.typeArgsParser.resolveQualified(name);
    }
    String fqn = ctx.importMap.get(name);
    if (fqn != null) return fqn;
    if (ctx.currClazz != null && name.equals(ctx.currClazz.name)) return ctx.currClazz.fqn();
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

  private boolean isNewKeyword(Token curr) {
    return (curr instanceof Keyword && ((Keyword) curr).eq(keyword("new")));
  }

  private boolean isInstanceofKeyword(Token curr) {
    return (curr instanceof Keyword && ((Keyword) curr).eq(keyword(Keywords.INSTANCEOF)));
  }

  private Token flattenId(List<Identifier> selectors) {
    Identifier id = selectors.get(0);
    Identifier root = id;
    for (int i = 1; i < selectors.size(); i++) {
      // a MethodCall selector may already carry a chain, e.g. "a.get(1).get(2)" parsed as one
      // MethodCall. (Only MethodCalls: plain Identifier selectors are lexer tokens that may still
      // carry a stale .next link from an abandoned parse attempt, which must be overwritten.)
      while (id instanceof MethodCall && id.next != null) {
        id = id.next;
      }
      id.next = selectors.get(i);
      id = id.next;
    }
    return root;
  }

  private MethodCall parseMethod(TokenList tokens) {
    if (tokens.curr() instanceof Identifier) {
      Identifier id = ((Identifier) tokens.curr());
      tokens.next();
      ArraySelector sel = ctx.arraySelectorParser.parse(tokens, true);
      if (sel != null) {
        id.arrSel = sel;
      } else {
        tokens.prev();
      }
    }
    MethodCall methodCall = ctx.methodCallParser.parse(tokens, true);
    if (methodCall != null) {
      return methodCall;
    }
    return null;
  }

  private boolean checkNegative(
      TokenList tokens, List<Token> exprTokens, int[] pendingNegatedParens) {
    if (tokens.curr().eq(op(MINUS))) {
      tokens.next();
      if (tokens.curr() instanceof Num) {
        Num number = (Num) tokens.curr();
        number.changeSign();
        exprTokens.add(tokens.curr());
        tokens.next();
        return true;
      } else if (tokens.curr() instanceof Identifier) {
        exprTokens.add(sym(LPAREN));
        exprTokens.add(integer(-1));
        exprTokens.add(op(TIMES));
        exprTokens.add(tokens.curr());
        exprTokens.add(sym(RPAREN));
        tokens.next();
        return true;
      } else if (tokens.curr().eq(sym(LPAREN))) {
        // Rewrite "-(expr)" as "(-1) * (expr)". The following '(' is left in place for the
        // caller's normal parenthesis handling to parse "expr" (preserving dotted selectors,
        // method calls, etc.); a matching synthetic ')' is appended once that '(' closes (see
        // pendingNegatedParens flush in parse()).
        exprTokens.add(sym(LPAREN));
        exprTokens.add(integer(-1));
        exprTokens.add(op(TIMES));
        pendingNegatedParens[0]++;
        return true;
      } else {
        ctx.errors.addError(
            LOG_NAME,
            "unexpected symbol "
                + tokens.curr()
                + " after starting \"-\" in expression (expected number literal or identifier)",
            new RuntimeException().getStackTrace());
      }
    }
    return false;
  }

  private boolean isHigerPrec(Operator op, Token sub) {
    return (sub instanceof Operator && ((Operator) sub).precedence() >= op.precedence());
  }

  // shunting yard algorithm to transform the token list to postfix notation
  private List<Token> postfix(List<Token> tokens) {
    List<Token> output = new ArrayList<>();
    Deque<Token> stack = new LinkedList<>();
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (token instanceof Operator) {
        while (!stack.isEmpty() && isHigerPrec((Operator) token, stack.peek())) {
          output.add(stack.pop());
        }
        stack.push(token);
      } else if (token.eq(sym(LPAREN))) {
        stack.push(token);
      } else if (token.eq(sym(RPAREN))) {
        while (!stack.peek().eq(sym(LPAREN))) {
          output.add(stack.pop());
        }
        stack.pop();
      } else {
        output.add(token);
      }
    }
    while (!stack.isEmpty()) {
      output.add(stack.pop());
    }
    return output;
  }
}
