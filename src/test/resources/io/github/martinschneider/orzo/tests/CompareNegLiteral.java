package io.github.martinschneider.orzo.tests;

public class CompareNegLiteral {
  public static void main(String[] args) {
    short x = -1;
    int y = -1;
    if (x == -1) {
      System.out.println("true");
    } else {
      System.out.println("false");
    }
    if (y == -1) {
      System.out.println("true");
    } else {
      System.out.println("false");
    }
    if (x == -2) {
      System.out.println("true");
    } else {
      System.out.println("false");
    }
    if (y != -1) {
      System.out.println("true");
    } else {
      System.out.println("false");
    }
    if (y > -5) {
      System.out.println("true");
    } else {
      System.out.println("false");
    }
  }
}
