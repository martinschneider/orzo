package io.github.martinschneider.orzo.tests;

public class StaticCallOnClassName {
  public static void main(String[] args) {
    // the argument types must be known to pick println(String), not println(int)
    System.err.println(String.valueOf(1));
    System.out.println(String.valueOf(2));
    System.out.println(String.format("%s-%s", "x", 2));
    // the rest of a chain after a static call on a class name
    int n = String.format("%s", "abc").length();
    System.out.println(n);
  }
}
