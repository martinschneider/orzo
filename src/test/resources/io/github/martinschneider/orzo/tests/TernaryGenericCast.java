package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;
public class TernaryGenericCast {
  private Map<String, StringBuilder> map = new HashMap<>();
  void run(String k, boolean b) {
    StringBuilder sb = (b && map != null) ? map.get(k) : null;
    if (sb != null) {
      System.out.println(sb.length());
    }
  }
  public static void main(String[] args) {
    TernaryGenericCast r = new TernaryGenericCast();
    r.map.put("k", new StringBuilder("abc"));
    r.run("k", true);
  }
}
