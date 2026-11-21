package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;

public class NestedClassTypes {
  static String key(Map.Entry<String, Integer> e) {
    return e.getKey() + "=" + e.getValue();
  }

  public static void main(String[] args) {
    Map<String, Integer> m = new HashMap<>();
    m.put("a", 1);
    for (Map.Entry<String, Integer> e : m.entrySet()) {
      System.out.println(key(e));
      java.util.Map.Entry<String, Integer> same = e;
      System.out.println(same.getKey().length());
    }
  }
}
