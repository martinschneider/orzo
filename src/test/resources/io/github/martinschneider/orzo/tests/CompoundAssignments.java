package io.github.martinschneider.orzo.tests;

public class CompoundAssignments {
  public static void main(String[] args) {
    int a = 100;
    a += 1;
    System.out.println(a); // 101
    a *= 2;
    System.out.println(a); // 202
    a -= 3;
    System.out.println(a); // 199
    a /= 4;
    System.out.println(a); // 49
    a %= 5;
    System.out.println(a); // 4
    a <<= 6;
    System.out.println(a); // 256
    a >>= 7;
    System.out.println(a); // 2
    a >>>= 8;
    System.out.println(a); // 0
    short s = 32760;
    s += 10;
    System.out.println(s); // -32766 (wraps: implicit narrowing cast on compound assignment)
    byte b = 120;
    b += 10;
    System.out.println(b); // -126 (wraps)
  }
}
