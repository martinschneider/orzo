package io.github.martinschneider.orzo.tests;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MethodSignature {
  static int count(List<String> values, Predicate<String> filter) {
    int n = 0;
    for (String v : values) {
      if (filter.test(v)) {
        n++;
      }
    }
    return n;
  }

  static int size(Map<String, int[]> map, int extra) {
    return map.size() + extra;
  }

  static void print(String name) {
    for (java.lang.reflect.Method m : MethodSignature.class.getDeclaredMethods()) {
      if (m.getName().equals(name)) {
        System.out.println(m.toGenericString());
      }
    }
  }

  public static void main(String[] args) {
    // the class file keeps the generic parameter types (Signature attribute)
    print("count");
    print("size");
  }
}
