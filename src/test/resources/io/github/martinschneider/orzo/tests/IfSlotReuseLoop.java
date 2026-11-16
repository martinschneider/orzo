package io.github.martinschneider.orzo.tests;

public class IfSlotReuseLoop {
  public static void main(String[] args) {
    if (true) {
      int a = 0;
      while (a < 3) {
        a++;
      }
      System.out.println(a);
    }
    if (true) {
      int b = 10;
      System.out.println(b);
    }
  }
}
