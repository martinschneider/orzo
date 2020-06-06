package io.github.martinschneider.orzo.tests;

public class UnsignedRightShift {
  public static void main(String[] args) {
    int a = -1;
    for (int i = 0; i < 31; i++) {
      a = a >>> 1;
      System.out.println(a);
    }
  }
}
