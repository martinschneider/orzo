package io.github.martinschneider.orzo.tests;

public class CastPrecedence {
  public static void main(String[] args) {
    // a cast applies to its operand only, also at the start of an expression
    double d = 1.5;
    double product = (int) d * 1.5;
    System.out.println(product);
    System.out.println((int) d * 2);
    int a = 7;
    int b = 2;
    System.out.println((double) a / b);
    int m = 1000000;
    System.out.println((long) m * m);
    // casts to reference types inside an expression
    Object boxed = 41;
    System.out.println((Integer) boxed + 1);
    Object text = "x";
    System.out.println((String) text + "!");
    System.out.println(1 + (Integer) boxed);
  }
}
