package io.github.martinschneider.orzo.tests;

public class TryCatchInIf {
  public static int safeDivide(int a, int b, boolean safe) {
    if (safe) {
      try {
        return a / b;
      } catch (ArithmeticException e) {
        return -1;
      }
    }
    return -2;
  }

  public static void main(String[] args) {
    System.out.println(safeDivide(10, 2, true));
    System.out.println(safeDivide(10, 0, true));
    System.out.println(safeDivide(10, 0, false));
  }
}
