package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;

import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Symbols;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ShuntingYard {
  private static final Symbols RPAREN = null;

  private boolean isHigerPrec(Operator op, Token sub) {
    return (sub instanceof Operator && ((Operator) sub).precedence() >= op.precedence());
  }

  public List<Token> postfix(List<Token> tokens) {
    List<Token> output = new ArrayList<>();
    Deque<Token> stack = new LinkedList<>();
    for (Token token : tokens) {
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
        // digit
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
