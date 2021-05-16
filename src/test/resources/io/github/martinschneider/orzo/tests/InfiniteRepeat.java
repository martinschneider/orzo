package io.github.martinschneider.orzo.tests;

public class InfiniteRepeat {
  public static void main(String[] args) {
    repeat {
      System.out.println("enter");
      break;
    }
    System.out.println("exit");
  }
}