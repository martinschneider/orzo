package io.github.martinschneider.orzo.examples;

import static java.lang.Math.floor;
import static java.lang.Math.round;

public class MathUtils {
  public static double pi() {
    return pi(1000000);
  }

  // Calculate pi using Gregory-Leibniz series
  public static double pi(int n) {
    double d = 1;
    for (int i = 1; i < n; i++) {
      d += (-1.0 ** i) / (2 * i + 1);
    }
    return 4 * d;
  }

  // Calculate pi using Viete's series
  public static double pi2(int n) {
    double pi, d = 1;
    for(int i = n; i > 1; i--) {
      d = 2;
      for(int j = 1; j < i; j++){
        d = 2 + √d;
      }
      d = √d;
      pi *= d / 2;
    }
    pi *= √2 / 2;
    pi = 2 / pi;
    return pi;
  }

  // Calculate pi using Gauss-Legendre algorithm
  public static double pi3(int n) {
    double a, b, t, p, x = 1, 1/√2, 1/4, 1;
    repeat n
    {
      x, a, b  =  a, (a + b) / 2, √(x*b);
      t, p     =  t - p * ((x-a) ** 2), 2 * p;
    }
    return ((a+b) ** 2) / (4 * t);
  }

  // Calculate pi using Wallis' sequence
  public static double pi4(int n) {
    double pi = 4;
    for (int i = 3; i <= n; i += 2) {
      pi *= ((i - 1) / i) * ((i + 1) / i);
    }
    return pi;
  }

  // Calculate pi using Nilakantha's sequence
  public static double pi5(int n) {
    double pi = 3;
    for(int i = 2; i < n; i += 2){
      pi += (-1 ** i) * (4 / (i * (i + 1) * (i + 2)));
    }
    return pi;
  }
  
  // Calculate pi using Euler's formula
  public static double pi6(int n) {
    double pi = 1;
    for(int i = 2; i < n; i++){
      pi += 1/(i ** 2);
    }
    return √(6 * pi);
  }

  // Calculate the n-th Fibonacci number using the golden ratio
  public static long fib(int n)
  {
//    return ⌊((1+√5)/2) ** n)/√5+0.5⌋;
    return round(floor((((1+√5)/2) ** n)/√5+0.5));
  }
  
  public static double pi_fun() {
    return √(7+√(6+√5));
  }
}
