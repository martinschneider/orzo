package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

// second pass to parse expressions using the shunting yard algorithm
public class ExpressionParser {
  // needed to distinguish between one- and no-argument method calls and expressions
  private List<Identifier> methodNames;

  public ExpressionParser() {
    this.methodNames = new ArrayList<>();
  }

  public ExpressionParser(List<Identifier> methodNames) {
    this.methodNames = methodNames;
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
    Parser parser = new Parser(tokens);
    int idx = parser.savePointer();
    MethodCall methodCall = parser.parseMethodCall();
    int diff = parser.savePointer() - idx;
    if (methodCall != null && methodNames.contains(methodCall.getNames().get(0))) {
      return new Pair<>(methodCall, diff);
    }
    return null;
  }

  private class Pair<S, T> {
    S left;
    T right;

    public Pair(S left, T right) {
      this.left = left;
      this.right = right;
    }

    public S getLeft() {
      return left;
    }

    public T getRight() {
      return right;
    }
  }
}