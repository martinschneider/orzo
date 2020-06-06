package io.github.martinschneider.orzo.tests;

public class Longs {
  public static void main(String[] args) {
    long a = 2147483647;
    a++;
    System.out.println(a);
    long b = -2147483648;
    b--;
    System.out.println(b);
    long c = 10000000000L;
    System.out.println(c);
    System.out.println(times2(c));
    for (long i = 0; i < 5; i++) {
      System.out.println(i + 1);
    }
    long d = 6;
    while (d <= 10) {
      System.out.println(d);
      d++;
    }
    d = 9;
    do {
      System.out.println(d);
      d--;
    } while (d >= 0);
  }

  public static long times2(long a) {
    return 2 * a;
  }
}
