package io.github.martinschneider.orzo.tests;

public class MultiCatch {
  static String parse(String s, int idx) {
    try {
      return "ok " + Integer.parseInt(s.substring(idx));
    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
      return "caught " + e.getClass().getSimpleName();
    }
  }

  public static void main(String[] args) {
    System.out.println(parse("x12", 1));
    System.out.println(parse("xab", 1));
    System.out.println(parse("x", 5));
    try {
      Object o = null;
      o.toString();
    } catch (IllegalStateException | NullPointerException e) {
      System.out.println("npe");
    }
    System.out.println("end");
  }
}
