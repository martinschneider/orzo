package io.github.martinschneider.orzo.tests;

public class TernaryNoParen {
  static int toInt(boolean v) {
    return (v) ? 1 : 0;
  }

  static int max(int a, int b) {
    return a > b ? a : b;
  }

  static int clamp(int x, int lo, int hi) {
    int low = x < lo ? lo : x;
    return low > hi ? hi : low;
  }

  public static void main(String[] args) {
    System.out.println(toInt(true));
    System.out.println(toInt(false));
    System.out.println(max(3, 5));
    System.out.println(max(7, 2));
    System.out.println(clamp(10, 0, 5));
    System.out.println(clamp(-1, 0, 5));
    System.out.println(clamp(3, 0, 5));
  }
}
