package io.github.martinschneider.orzo.tests;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntBinaryOperator;

public class LambdaArguments {
  static double offset = 0.5;

  // a lambda as an argument of an own method
  static double twice(DoubleUnaryOperator f, double x) {
    return f.applyAsDouble(f.applyAsDouble(x));
  }

  static int fold(int[] values, IntBinaryOperator op) {
    int result = values[0];
    for (int i = 1; i < values.length; i++) {
      result = op.applyAsInt(result, values[i]);
    }
    return result;
  }

  // a lambda as a return value (capturing a parameter)
  static DoubleUnaryOperator scale(double factor) {
    return x -> x * factor;
  }

  public static void main(String[] args) {
    System.out.println(twice(x -> x + 1, 1.0));
    System.out.println(twice(x -> x * x, 3));
    System.out.println(fold(new int[] {3, 1, 4, 1, 5}, (a, b) -> Math.max(a, b)));
    System.out.println(twice(scale(2.5), 1));
    // a static field in a lambda body
    System.out.println(twice(x -> x + offset, 0));
  }
}
