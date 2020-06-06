package io.github.martinschneider.orzo.tests;

public class BitOperators {
  public static void main(String[] args) {
    System.out.println(0 & 1);
    System.out.println(2 ^ 3);
    System.out.println(4 | 5);
    long a = 0;
    a &= 1;
    long b = 2;
    b ^= 3;
    long c = 4;
    c |= 5;
    System.out.println(a);
    System.out.println(b);
    System.out.println(c);
  }
}
