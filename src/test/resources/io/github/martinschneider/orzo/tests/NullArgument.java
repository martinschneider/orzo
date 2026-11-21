package io.github.martinschneider.orzo.tests;

public class NullArgument {
  static String describe(String s, int n) {
    return (s == null ? "none" : s) + n;
  }

  static String describe(int n) {
    return "int " + n;
  }

  static String pick(StringBuilder sb) {
    return sb == null ? "null builder" : sb.toString();
  }

  public static void main(String[] args) {
    System.out.println(describe(null, 1));
    System.out.println(describe("x", 2));
    System.out.println(pick(null));
    StringBuilder b = new StringBuilder(String.valueOf((Object) null));
    System.out.println(b);
  }
}
