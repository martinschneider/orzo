package io.github.martinschneider.orzo.tests;
public class LongIntComparison {
  public static void main(String[] args) {
    long big = 3000000000L;
    long small = 5L;
    if (big > Integer.MAX_VALUE) {
      System.out.println("big exceeds MAX_VALUE");
    }
    if (small > Integer.MAX_VALUE) {
      System.out.println("should not print");
    }
    if (Integer.MAX_VALUE > small) {
      System.out.println("MAX_VALUE exceeds small");
    }
  }
}
