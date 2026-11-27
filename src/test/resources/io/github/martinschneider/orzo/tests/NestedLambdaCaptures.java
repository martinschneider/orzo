package io.github.martinschneider.orzo.tests;

public class NestedLambdaCaptures {
  // lambdas in lambdas capturing the parameters (with declared types) and locals of the lambdas
  // around them
  interface TU<T, U> {
    T foo(U u);
  }

  static <T, U> T exec(TU<T, U> lambda, U x) {
    return lambda.foo(x);
  }

  int i = 40;

  int test(final int a0) {
    return exec(
        (final Integer a1) -> {
          final Integer x2 = 10;
          return exec(
              (final Integer a2) -> {
                final Integer x3 = 20;
                return exec((Integer a3) -> a0 + a1 + a2 + a3 + x2 + x3 + i, 3);
              },
              2);
        },
        1);
  }

  public static void main(String[] args) {
    System.out.println(new NestedLambdaCaptures().test(30));
  }
}
