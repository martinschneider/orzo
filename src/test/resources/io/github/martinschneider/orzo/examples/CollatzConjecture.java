package io.github.martinschneider.orzo.examples;

public class CollatzConjecture {
  public static void main(String[] args) {
    int n = 97;
    while (n > 1) {
      System.out.println(n);
      n = next(n);
    }
    System.out.println(n);
  }

  public static int next(int n) {
    if (n % 2 == 0) {
      return n / 2;
    }
    return 3 * n + 1;
  }
}
