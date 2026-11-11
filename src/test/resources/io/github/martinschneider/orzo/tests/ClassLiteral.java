package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;

public class ClassLiteral {
  static String describe(Class c, String label) {
    return label + ":" + c;
  }

  public static void main(String[] args) {
    Class c = String.class;
    System.out.println(c);
    System.out.println(describe(Integer.class, "x"));
    Map<Class, String> m = new HashMap<>();
    m.put(String.class, "hello");
    System.out.println(m.get(String.class));
  }
}
