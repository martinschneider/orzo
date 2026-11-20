package io.github.martinschneider.orzo.tests;

public class WhileTrueBreakInTry {
  private static int sumTo(int target) {
    int sum = 0;
    int i = 0;
    while (true) {
      try {
        if (i >= target) {
          break;
        }
        sum = sum + i;
        i = i + 1;
      } catch (ArithmeticException e) {
        break;
      }
    }
    return sum;
  }

  public static void main(String[] args) {
    System.out.println(sumTo(5));
  }
}
