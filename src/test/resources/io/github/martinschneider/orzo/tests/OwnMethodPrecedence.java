package io.github.martinschneider.orzo.tests;

public class OwnMethodPrecedence {
  private static String describe(int n) {
    return "own " + n;
  }

  public static void main(String[] args) {
    // OwnMethodPrecedenceOther (compiled in the same run) has a private static describe(int)
    System.out.println(describe(1));
    System.out.println(OwnMethodPrecedenceOther.run());
  }
}
