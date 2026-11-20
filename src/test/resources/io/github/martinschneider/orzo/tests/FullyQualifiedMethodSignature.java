package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;

public class FullyQualifiedMethodSignature {
  static java.lang.StringBuilder make(String s) {
    return new StringBuilder(s);
  }

  static int total(java.util.List<String> items, java.lang.String sep) {
    return items.get(0).length() + sep.length();
  }

  public static void main(String[] args) {
    System.out.println(make("abc").append("d").length());
    java.util.List<String> l = new ArrayList<>();
    l.add("hello");
    System.out.println(total(l, ", "));
  }
}
