package io.github.martinschneider.orzo.tests;

public class ConcatComparison {
  public static void main(String[] args) {
    // comparisons and logical operations as operands of a string concatenation
    int a = 1;
    int b = 1;
    String s = "x";
    System.out.println("equal: " + (a == b));
    System.out.println("less: " + (a < 0) + ", both: " + (a == b && s != null));
    System.out.println((a != b) + " " + !(a == b));
  }
}
