package io.github.martinschneider.orzo.tests;

public class InfiniteRepeat {
  public static void main(String[] args) {
    for (;;) {
      System.out.println("for");
      break;
    }
    while(true) {
      System.out.println("while");
      break;
    }
    do {
      System.out.println("do");
      break;
    }
    while(true)
    repeat {
      System.out.println("repeat");
      break;
    }
    System.out.println("exit");
  }
}