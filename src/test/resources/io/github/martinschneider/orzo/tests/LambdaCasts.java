package io.github.martinschneider.orzo.tests;

// casts of lambdas and method references (also to intersection types), parenthesized lambdas
import java.io.Serializable;
import java.util.function.*;

public class LambdaCasts {
  interface SAM { int m(int x); }
  static int twice(int x) { return 2 * x; }
  static int call(SAM s) { return s.m(5); }
  public static void main(String[] args) {
    Object o = (Runnable) () -> System.out.println("run");
    ((Runnable) o).run();
    SAM s = (SAM) x -> x + 1;
    System.out.println(s.m(1) + " " + ((SAM) x -> x * 3).m(2));
    Runnable r = (Runnable & Serializable) () -> System.out.println("ser");
    r.run();
    Object q = (Serializable & SAM) x -> -x;
    System.out.println(((SAM) q).m(4) + " " + (q instanceof Serializable));
    SAM t = (x -> x - 1);
    System.out.println(t.m(10) + " " + call((SAM) LambdaCasts::twice) + " " + call((x -> x)));
    Supplier<String> sup = (Supplier<String>) () -> "sup";
    System.out.println(sup.get());
  }
}
