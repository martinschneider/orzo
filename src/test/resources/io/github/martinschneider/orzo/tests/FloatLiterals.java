package io.github.martinschneider.orzo.tests;

public class FloatLiterals {
  public static void main(String[] args) {
    // floating-point literals with an exponent or a suffix
    double small = 1e-6;
    double large = 2.5E3;
    float f = 1.5e+2f;
    System.out.println(small * 1000000);
    System.out.println(large);
    System.out.println(f);
    System.out.println(2f + 3d);
  }
}
