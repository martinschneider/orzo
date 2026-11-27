package io.github.martinschneider.orzo.tests;

import java.util.function.BiFunction;
import java.util.function.Function;

public class NullConcatenation {
  // a null String operand of a concatenation is "null" (also in lambda bodies), and a negated
  // comparison in a lambda body's condition
  static String none() {
    return null;
  }

  // (a lambda using the enclosing instance)
  boolean flag;
  String name;

  void inner(int n) {
    Runnable r =
        () -> {
          if (!flag && !(n > 3)) {
            System.out.println("inner " + name + (!flag && !(n < 3)) + name);
          }
        };
    r.run();
  }

  public static void main(String[] args) {
    String s = null;
    long l = 5;
    Object o = null;
    System.out.println("a" + s + s + 1 + o);
    System.out.println(s + "b" + none());
    System.out.println(s + s);
    Function<String, String> f = x -> x + "!" + x + l;
    System.out.println(f.apply(null));
    BiFunction<String, String, String> g = (x, y) -> x + y;
    System.out.println(g.apply("p", "q") + g.apply(null, null));
    System.out.println(l + s);
    boolean b = false;
    int n = Integer.parseInt("2");
    Runnable r =
        () -> {
          if (!b && !(n > 3)) {
            System.out.println("few " + (!b && !(n < 3)));
          }
        };
    r.run();
    new NullConcatenation().inner(2);
  }
}
