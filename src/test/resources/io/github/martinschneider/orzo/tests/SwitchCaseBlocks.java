package io.github.martinschneider.orzo.tests;

public class SwitchCaseBlocks {
  static String describe(int n) {
    switch (n) {
      case 1:
        {
          String s = "one";
          return s;
        }
      case 2:
        {
          String s = "two";
          if (s.length() > 0) {
            return s + "!";
          }
          return s;
        }
      default:
        {
          return "other";
        }
    }
  }

  public static void main(String[] args) {
    System.out.println(describe(1));
    System.out.println(describe(2));
    System.out.println(describe(3));
  }
}
