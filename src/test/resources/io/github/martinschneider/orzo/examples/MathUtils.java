package io.github.martinschneider.orzo.examples;

import static java.lang.Math.floor;
import static java.lang.Math.round;

public class MathUtils {
  public static double times(double a, double b) {
    return a * b;
  }

  public static double times(int a, int b) {
    return a * b;
  }

  public static double pi() {
    return pi(1000000);
  }
  
  public static double pi(int n) {
    // Calculate pi using the Leibniz series
    double d = 1;
    for (int i = 1; i < n; i++) {
      d += (-1.0 ** i) / (2 * i + 1);
    }
    return 4 * d;
  }
  
  public static double pi2() {
    return √(7+√(6+√5));
  }
  
  public static long fib(int n)
  {
    return round(floor((((1+√5)/2) ** n)/√5+0.5));
  }
}
