package io.github.martinschneider.orzo.tests;

// instanceof with type patterns ("o instanceof String s")
import java.util.*;

public class TypePatterns {
  static String describe(Object o) {
    if (o instanceof String s) {
      return "string " + s.length();
    } else if (o instanceof Integer i && i > 10) {
      return "big " + (i + 1);
    } else if (o instanceof Integer i) {
      return "int " + i;
    }
    if (!(o instanceof List<?> l)) {
      return "other";
    }
    return "list " + l.size();
  }

  static int len(Object o) {
    return o instanceof String s ? s.length() : -1;
  }

  public static void main(String[] args) {
    System.out.println(describe("abc"));
    System.out.println(describe(42));
    System.out.println(describe(7));
    System.out.println(describe(List.of(1, 2)));
    System.out.println(describe(null));
    System.out.println(len("four") + " " + len(3));
    Object x = "q";
    boolean b = x instanceof final String t && t.equals("q");
    System.out.println(b);
    for (Object o : List.of("a", 1, 2.5)) {
      if (o instanceof Number n) System.out.println(n.intValue());
    }
    Runnable r = () -> { if (x instanceof String s2) System.out.println(s2 + "!"); };
    r.run();
  }
}
