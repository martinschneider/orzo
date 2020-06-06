package io.github.martinschneider.orzo.tests;

public class BitShifts {
  public static void main(String[] args) {
    int a = 1;
    for (int i = 1; i < 31; i++) {
      a = a << 1;
      System.out.println(a);
    }
    int c = a;
    long b = a;
    for (int i = 1; i < 31; i++) {
      b = b << 1;
      System.out.println(b);
    }
    for (int i = 1; i < 31; i++) {
      b = b >> 1;
      System.out.println(b);
    }
    a = c; // replace with `a = (int)b;` once we support casting
    for (int i = 1; i < 31; i++) {
      a = a >> 1;
      System.out.println(a);
    }
  }
}
