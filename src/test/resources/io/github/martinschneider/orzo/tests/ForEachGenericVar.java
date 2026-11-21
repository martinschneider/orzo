package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;
public class ForEachGenericVar {
  public static void main(String[] args) {
    Map<String, String> m = new HashMap<>();
    m.put("ab", "cd");
    for (Map.Entry<String, String> e : m.entrySet()) {
      System.out.println(e.getKey().startsWith("a") + " " + e.getValue().length());
    }
  }
}
