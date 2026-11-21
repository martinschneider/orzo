package io.github.martinschneider.orzo.tests;

import java.util.ArrayDeque;
import java.util.Deque;
public class UnboxGenericReturn {
  public static void main(String[] args) {
    Deque<Integer> d = new ArrayDeque<>();
    d.push(4);
    d.push(7);
    int top = d.pop();
    int x = 0;
    x = d.pop();
    System.out.println(top + x);
  }
}
