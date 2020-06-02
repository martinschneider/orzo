package io.github.martinschneider.orzo.examples;

public class Factorial {
  // inefficient code, do not do this ;-)
  public static void main(String[] args) {
    for (int i = 1; i <= 10; i++) {
      System.out.println(fac(i));
    }
  }

  public static int fac(int n) {
    if (n == 1) {
      return 1;
    }
    return n * fac(n - 1);
  }
}
