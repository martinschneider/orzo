package io.github.martinschneider.orzo.tests;

public class ComparisonAsValue {
  public static void main(String[] args) {
    int h1 = 5;
    int h2 = 5;
    System.out.println(h1 == h2);
    boolean same = h1 == h2;
    System.out.println(same);
    boolean diff = h1 == 6;
    System.out.println(diff);
    if (h1 == h2) {
      System.out.println("branch-still-works");
    }
    System.out.println(h1 == h2 && h2 == h1);
  }
}
