package io.github.martinschneider.orzo.tests;

public class Pow {
  public static void main(String[] args) {
    System.out.println(2.0 ** 4.0); // 16.0
    System.out.println(2 ** 4); // 16
    System.out.println(2 ** 4.0); // 16.0
    System.out.println(2.0 ** 4); // 16.0
    int a = 3;
    int b = 3;
    System.out.println(a ** b); // 27
    double c = 2.0;
    System.out.println(c ** b); // 8.0
    System.out.println((7.0 ** 7)/(4 ** 9)); // 3.1415672302246094
    System.out.println(((((5 ** 0.5)+6) ** 0.5) + 7) ** 0.5); // 3.1416325445036177
  }
}
