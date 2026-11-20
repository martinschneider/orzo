package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.HashMap;

public class FullyQualifiedGenericDeclaration {
  public static void main(String[] args) {
    System.out.println("start");
    java.util.Map<String, Integer> m = new HashMap<>();
    m.put("x", 1);
    System.out.println(m.size());
    java.util.List<java.util.List<String>> l = new ArrayList<>();
    java.util.Map<String, Integer> m2 = new java.util.HashMap<>(m);
    System.out.println(m2.get("x"));
    java.util.List<String> l2 = new java.util.ArrayList<String>();
    l2.add("y");
    System.out.println(l.size());
    System.out.println(l2.size());
    System.out.println(new java.lang.StringBuilder("s").append(1).toString());
    System.out.println("end");
  }
}
