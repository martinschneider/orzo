package io.github.martinschneider.orzo.tests;

public class ArrayLengthCast {
  public static short toShort(short x) {
    return x;
  }

  public static void main(String[] args) {
    byte[] data = new byte[42];
    System.out.println(toShort((short) data.length));
  }
}
