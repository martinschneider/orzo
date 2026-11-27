package io.github.martinschneider.orzo.tests;

public class MultipleDeclarators {
  // Java's "int a, b = 1;" (only b has a value) next to Orzo's parallel "int a, b = 1, 2;"
  static int f, g = 3;

  public static void main(String[] args) {
    int a, b = 1, c = 2;
    a = 5;
    int d, e = 4;
    d = 6;
    int p, q = 7, 8;
    System.out.println(a + " " + b + " " + c + " " + d + " " + e + " " + p + " " + q);
    System.out.println(f + " " + g);
  }
}
