package io.github.martinschneider.orzo.examples;

public class CollatzConjecture2 {
  public static void main(String[] args) {
    // the collatz sequence for 942488749153153 has 1862 steps
    long n = 942488749153153l;
    int c = 0;
    do {
      c++;
      if (n % 2 == 0) {
        n = n / 2;
      } else {
        n = 3 * n + 1;
      }
    } while (n > 1);
    System.out.println(c); // 1862
  }
}
