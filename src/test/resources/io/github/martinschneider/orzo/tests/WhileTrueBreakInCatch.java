package io.github.martinschneider.orzo.tests;

public class WhileTrueBreakInCatch {
  private static int sumUntilError() {
    int sum = 0;
    int divisor = 2;
    while (true) {
      try {
        sum = sum + 100 / divisor;
        divisor = divisor - 1;
      } catch (ArithmeticException e) {
        break;
      }
    }
    return sum;
  }

  public static void main(String[] args) {
    System.out.println(sumUntilError());
  }
}
