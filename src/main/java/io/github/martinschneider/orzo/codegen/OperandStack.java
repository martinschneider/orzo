package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT_ZERO;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;

import java.util.Deque;
import java.util.LinkedList;

public class OperandStack {
  private int maxSize;
  private int size;
  private Deque<String> types = new LinkedList<>();

  public void push(String type) {
    if (!type.equals(VOID)) {
      types.push(type);
      size++;
      if (type.equals(LONG) || type.equals(DOUBLE)) {
        size++;
      }
      if (size > maxSize) {
        maxSize = size;
      }
    }
  }

  public String pop() {
    String ret = null;
    if (types.peek() != null) {
      if ((types.peek().equals(LONG) || types.peek().equals(DOUBLE))) {
        size--;
      }
      size--;
      ret = types.pop();
    }
    return ret;
  }

  public String pop2() {
    pop();
    return pop();
  }

  public void pop(int n) {
    for (int i = 0; i < n; i++) {
      pop();
    }
  }

  public String type() {
    return types.peek();
  }

  public int maxSize() {
    return maxSize;
  }

  public void reset() {
    maxSize = 0;
    types.clear();
  }

  // one and only one!
  // TODO: ideally this would be written as:
  //   (tmp != null && tmp.equals(INT_ZERO)) ^ (types.peek() != null &&
  // types.peek().equals(INT_ZERO))
  // but Orzo does not yet correctly compile compound null-check conditions of the form
  //   if (x != null && x.method()) { ... }
  // (the generated if_acmpeq instruction is missing its 2-byte branch offset operand).
  // Fix the code generator to support this pattern, then simplify this method.
  public boolean oneOfTopTwoElementsIsZero() {
    boolean first = false;
    boolean second = false;
    String tmp = types.pop();
    if (tmp != null) {
      if (tmp.equals(INT_ZERO)) {
        first = true;
      }
    }
    if (types.peek() != null) {
      if (types.peek().equals(INT_ZERO)) {
        second = true;
      }
    }
    types.push(tmp);
    return first ^ second;
  }
}
