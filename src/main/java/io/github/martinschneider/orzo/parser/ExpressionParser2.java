package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.util.Pair;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

// second pass to parse expressions using the shunting yard algorithm
public class ExpressionParser2 {
  // needed to distinguish between one- and no-argument method calls and expressions
  private List<Identifier> methodNames;
  private CGContext ctx;

  public ExpressionParser2(CGContext ctx) {
    this.methodNames = new ArrayList<>();
    this.ctx = ctx;
  }

  public ExpressionParser2(CGContext ctx, List<Identifier> methodNames) {
    this.methodNames = methodNames;
    this.ctx = ctx;
  }

  private boolean isHigerPrec(Operator op, Token sub) {
    return (sub instanceof Operator && ((Operator) sub).precedence() >= op.precedence());
  }

  public List<Token> postfix(List<Token> tokens) {
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
        List<Token> sublist = new ArrayList<>(tokens).subList(i, tokens.size() - 1);
        Pair<MethodCall, Integer> parseResult = parseMethodCall(sublist);
        if (parseResult != null) {
          MethodCall methodCall = parseResult.getLeft();
          output.add(methodCall);
          i += parseResult.getRight();
        } else {
          output.add(token);
        }
      }
    }
    while (!stack.isEmpty()) {
      output.add(stack.pop());
    }
    return output;
  }

  private Pair<MethodCall, Integer> parseMethodCall(List<Token> tokens) {
    TokenList tokenList = new TokenList(tokens);
    int idx = tokenList.idx();
    MethodCall methodCall = ctx.parserCtx.methodCallParser.parse(tokenList);
    int diff = tokenList.idx() - idx;
    if (methodCall != null && methodNames.contains(methodCall.getName())) {
      return new Pair<>(methodCall, diff);
    }
    return null;
  }
}
