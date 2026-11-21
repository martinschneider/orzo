package io.github.martinschneider.orzo.tests;

public class OwnMethodPrecedenceOther {
  private static String describe(int n) {
    return "other " + n;
  }

  public static String run() {
    return describe(2);
  }
}
