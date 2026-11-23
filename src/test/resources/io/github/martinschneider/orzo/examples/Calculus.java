package io.github.martinschneider.orzo.examples;

import java.util.function.DoubleUnaryOperator;

// Numerical calculus with lambdas: Newton's method and Simpson's rule
public class Calculus {
  static double h = 1e-6;

  // f' (the central difference quotient)
  static DoubleUnaryOperator derivative(DoubleUnaryOperator f) {
    return x -> (f.applyAsDouble(x + h) - f.applyAsDouble(x - h)) ÷ (2 * h);
  }

  // a root of f near x (Newton's method)
  static double root(DoubleUnaryOperator f, double x) {
    DoubleUnaryOperator df = derivative(f);
    repeat 10 {
      x = x - f.applyAsDouble(x) / df.applyAsDouble(x);
    }
    return x;
  }

  // the integral of f from a to b (Simpson's rule with n intervals)
  static double integral(DoubleUnaryOperator f, double a, double b, int n) {
    double dx, sum, x = (b - a) ÷ n, f.applyAsDouble(a) + f.applyAsDouble(b), a;
    for (int i = 1; i < n; i++) {
      x += dx;
      sum += (i % 2 == 0 ? 2 : 4) * f.applyAsDouble(x);
    }
    return sum * dx ÷ 3;
  }

  public static void main(String[] args) {
    System.out.println(root(x -> x ** 2 - 2, 1) - √2);
    System.out.println(root(x -> x ** 2 - x - 1, 1));
    System.out.println(root(x -> Math.cos(x) - x, 1));
    System.out.println(integral(x -> 4 ÷ (1 + x ** 2), 0, 1, 100));
  }
}
