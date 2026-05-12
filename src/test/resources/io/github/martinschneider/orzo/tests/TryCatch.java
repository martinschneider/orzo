package io.github.martinschneider.orzo.tests;

public class TryCatch {
  public static int safeDivide(int a, int b) {
    try {
      return a / b;
    } catch (ArithmeticException e) {
      return -1;
    }
  }

  public static void main(String[] args) {
    System.out.println(safeDivide(10, 2));
    System.out.println(safeDivide(10, 0));
  }
}
