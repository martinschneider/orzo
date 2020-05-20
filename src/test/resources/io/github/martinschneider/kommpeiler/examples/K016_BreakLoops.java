package io.github.martinschneider.kommpeiler.examples;

public class K016_BreakLoops {
  public static void main(String[] args) {
    for (int i = 0; i < 1000; i++) {
      System.out.println(i);
      break;
    }
    int a = 1000;
    do {
      System.out.println(1);
      a--;
      break;
    } while (a > 0);
    int b = 1000;
    while (b > 0) {
      System.out.println(2);
      b--;
      break;
    }
  }
}
