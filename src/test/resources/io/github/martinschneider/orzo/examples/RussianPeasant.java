package io.github.martinschneider.orzo.examples;

public class RussianPeasant {
  public static void main(String[] args) {
    System.out.println(russianPeasant(37, 13));
  }

  public static int russianPeasant(int a, int b) {
    int n = 0;
    while (b > 0) {
      if ((b & 1) != 0) {
        n += a;
      }
      a <<= 1;
      b >>= 1;
    }
    return n;
  }
}
