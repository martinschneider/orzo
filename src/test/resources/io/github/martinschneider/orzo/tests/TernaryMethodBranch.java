package io.github.martinschneider.orzo.tests;

public class TernaryMethodBranch {
  static int doubled(int n) {
    return n * 2;
  }

  static int abs(int n) {
    return n >= 0 ? n : -n;
  }

  static int larger(int a, int b) {
    return a >= b ? doubled(a) : doubled(b);
  }

  public static void main(String[] args) {
    System.out.println(abs(3));
    System.out.println(abs(-7));
    System.out.println(larger(3, 5));
    System.out.println(larger(7, 2));
  }
}
