package io.github.martinschneider.orzo.tests;

public class NestedAlwaysReturns {
  static String classify(int n, boolean flag) {
    if (n > 0) {
      if (flag) {
        return "positive flagged";
      } else {
        return "positive";
      }
    } else {
      if (n == 0) {
        return "zero";
      } else if (flag) {
        return "negative flagged";
      } else {
        return "negative";
      }
    }
  }

  static int parse(String s) {
    try {
      if (s.isEmpty()) {
        return -1;
      } else {
        return Integer.parseInt(s);
      }
    } catch (NumberFormatException e) {
      return -2;
    }
  }

  public static void main(String[] args) {
    System.out.println(classify(1, true));
    System.out.println(classify(1, false));
    System.out.println(classify(0, false));
    System.out.println(classify(-1, true));
    System.out.println(classify(-1, false));
    System.out.println(parse(""));
    System.out.println(parse("7"));
    System.out.println(parse("x"));
  }
}
