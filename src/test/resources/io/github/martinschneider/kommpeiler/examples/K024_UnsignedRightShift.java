package io.github.martinschneider.kommpeiler.examples;

public class K024_UnsignedRightShift {
  public static void main(String[] args) {
    int a = -1;
    for (int i = 0; i < 31; i++) {
      a = a >>> 1;
      System.out.println(a);
    }
  }
}
