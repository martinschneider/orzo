package io.github.martinschneider.orzo.tests;

public class BooleanReturnArithmetic {
  static boolean f(int a, int b, int c) {
    return a - b == c;
  }
  static boolean g(byte a, int c) {
    return a - 10 == c;
  }
  public static void main(String[] args) {
    boolean x = 5 - 3 == 2;
    System.out.println(f(5, 3, 2) + " " + g((byte) 12, 2) + " " + x);
  }
}
