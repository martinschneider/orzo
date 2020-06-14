package io.github.martinschneider.orzo.examples;

public class MathUtils {
  public static double times(double a, double b) {
    return a * b;
  }

  public static double times(int a, int b) {
    return a * b;
  }

  public static double pi() {
    // Calculate pi using the Leibniz series
    double d = 1;
    for (int i = 1; i < 1000000; i++) {
      if (i % 2 == 0) {
        d += 1.0 / (2 * i + 1);
      } else {
        d -= 1.0 / (2 * i + 1);
      }
    }
    return 4 * d;
  }
}
