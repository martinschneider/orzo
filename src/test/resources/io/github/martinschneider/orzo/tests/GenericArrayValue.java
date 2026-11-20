package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;
public class GenericArrayValue {
  public static void main(String[] args) {
    Map<String, String[]> m = new HashMap<>();
    m.put("k", new String[] {"a", "b"});
    String[] a = m.get("k");
    if (a != null) {
      System.out.println(a[1]);
    }
  }
}
