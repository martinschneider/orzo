package io.github.martinschneider.orzo.tests;

public class ArrayMethodInit {
  public static void main(String[] args) {
    byte[] b = getBytes();
    System.out.println(b[0]);
    System.out.println(b[1]);
    int[] ints = getInts();
    System.out.println(ints[0]);
    System.out.println(ints[1]);
  }

  public static byte[] getBytes() {
    return new byte[] {42, 7};
  }

  public static int[] getInts() {
    return new int[] {100, 200};
  }
}
