package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class VoidReturnAfterCall {
  private List<String> items = new ArrayList<>();
  private boolean flag = true;

  void add(String s) {
    if (s.isEmpty()) {
      boolean b = flag;
      items.add(String.valueOf(b));
      return;
    }
    items.add(s);
  }

  public static void main(String[] args) {
    VoidReturnAfterCall v = new VoidReturnAfterCall();
    v.add("");
    v.add("x");
    System.out.println(v.items);
  }
}
