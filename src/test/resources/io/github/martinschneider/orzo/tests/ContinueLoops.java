package io.github.martinschneider.orzo.tests;

public class ContinueLoops {
  public static void main(String[] args) {
    for (int i = 0; i < 5; i++) {
      if (i % 2 == 0) {
        continue;
      }
      System.out.println(i);
    }
    int a = 0;
    while (a < 5) {
      a++;
      if (a % 2 == 0) {
        continue;
      }
      System.out.println(a);
    }
    int b = 0;
    do {
      b++;
      if (b % 2 == 0) {
        continue;
      }
      System.out.println(b);
    } while (b < 5);
  }
}
