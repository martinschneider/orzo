package io.github.martinschneider.orzo.tests;

import java.util.*;
import java.util.function.*;

public class MethodReferences {
  // constructor references and references on qualified class names
  static class Point { int x; Point(int x) { this.x = x; } public String toString() { return "P" + x; } }
  public static void main(String[] args) {
    Supplier<List<String>> make = ArrayList::new;
    List<String> l = make.get();
    l.add("a");
    Function<String, StringBuilder> sb = StringBuilder::new;
    Function<Integer, Point> p = Point::new;
    Map<String, Integer> m = new HashMap<>();
    m.put("k", 1);
    Function<Map.Entry<String, Integer>, String> key = Map.Entry::getKey;
    System.out.println(l + " " + sb.apply("x").append("y") + " " + p.apply(3) + " " + key.apply(m.entrySet().iterator().next()));
  }
}
