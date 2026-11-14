package io.github.martinschneider.orzo.tests;

public class EmptyBlocks {
  public static void main(String[] args) {
    int x = 5;
    if (x >= 2) {
    }
    System.out.println("after-empty-if");
    if (x == 1) {
    } else if (x >= 2) {
      System.out.println("else-if-taken");
    } else {
    }
    System.out.println("done");
  }
}
