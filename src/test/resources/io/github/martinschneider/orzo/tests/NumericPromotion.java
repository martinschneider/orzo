package io.github.martinschneider.orzo.tests;

public class NumericPromotion {
  static long f(long v) {
    return v;
  }

  public static void main(String[] args) {
    int x = 5;
    long l = 6L;
    if (x < l) {
      System.out.println("lt");
    }
    boolean b = l > x;
    System.out.println(b + " " + f(x - l));
    // mixed int/long/float/double operands in a string concatenation
    System.out.println("a" + (3 ^ l) + (x - l) + (l - x) + ~l + (l << x) + (x << l));
    System.out.println("b" + (x * 2.5) + (l / 2.0f));
  }
}
