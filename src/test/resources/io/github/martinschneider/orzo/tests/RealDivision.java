package io.github.martinschneider.orzo.tests;

public class RealDivision {
  static double half(double x) {
    return x / 2;
  }

  public static void main(String[] args) {
    // "÷" divides in floating point, "/" divides integers like in Java
    int a = 7;
    int b = 2;
    System.out.println(a / b);
    System.out.println(a ÷ b);
    System.out.println(1 ÷ 4);
    long big = 9;
    System.out.println(big ÷ 2 + 1);
    System.out.println(half(a ÷ b));
    if (a ÷ b > 3) {
      System.out.println("more than 3");
    }
    double x = 1;
    double y = 3;
    x, y = y ÷ 2, x ÷ 4;
    System.out.println(x + " " + y);
  }
}
