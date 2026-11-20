package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class WidenCallResult {
  static long big() {
    return 3L;
  }

  public static void main(String[] args) {
    List<String> l = new ArrayList<>();
    l.add("a");
    if (big() != l.size()) {
      System.out.println("mismatch");
    }
    long x = 1;
    System.out.println(x == l.size());
    double d = 0.5 + l.size();
    System.out.println(d);
  }
}
