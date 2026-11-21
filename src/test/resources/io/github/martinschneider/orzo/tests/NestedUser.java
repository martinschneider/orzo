package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;

public class NestedUser {
  public static void main(String[] args) {
    NestedClasses.Point p = new NestedClasses.Point(3, 4);
    Map<String, NestedClasses.Point> m = new HashMap<>();
    m.put("a", p);
    System.out.println(p.sum() + " " + m.get("a").x);
  }
}
