package io.github.martinschneider.orzo.tests;

public class Doubles {
  public static void main(String[] args) {
    double d1 = 23.0;
    double d2 = 17.0;
    System.out.println(d1 + d2);
    System.out.println(d1 - d2);
    System.out.println(d1 * d2);
    System.out.println(d1 / d2);
    System.out.println(d1 % d2);
    // TODO: fix
    // System.out.println(d1++);
    // System.out.println(d1--);
    d1 *= 4;
    System.out.println(d1);
    d1 /= 5;
    System.out.println(d1);
  }
}
