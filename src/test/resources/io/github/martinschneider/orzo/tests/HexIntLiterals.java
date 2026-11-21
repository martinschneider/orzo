package io.github.martinschneider.orzo.tests;

public class HexIntLiterals {
  static String describe(int i) {
    return "int " + i;
  }

  static String describe(long l) {
    return "long " + l;
  }

  public static void main(String[] args) {
    // 32-bit hex literals are ints (two's complement), not longs
    System.out.println(describe(0xCAFEBABE));
    System.out.println(0xFFFFFFFF);
    System.out.println(describe(0xFFFFFFFFL));
    System.out.println(0x7FFFFFFF);
  }
}
