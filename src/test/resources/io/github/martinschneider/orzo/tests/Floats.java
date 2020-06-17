package io.github.martinschneider.orzo.tests;

public class Floats {
  public static void main(String[] args) {
    float f1 = 23.0f;
    float f2 = 17.0F;
    System.out.println(f1 + f2);
    System.out.println(f1 - f2);
    System.out.println(f1 * f2);
    System.out.println(f1 / f2);
    System.out.println(f1 % f2);
    f1 = 3.14159f;
    System.out.println(f1);
    f1 *= 2;
    System.out.println(f1);
    f1 /= 3;
    System.out.println(f1);
  }
}
