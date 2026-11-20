package io.github.martinschneider.orzo.tests;

public class CStyleArrayParam {
  static int sum(int values[]) {
    int total = 0;
    for (int v : values) {
      total += v;
    }
    return total;
  }

  public static void main(String args[]) {
    System.out.println(sum(new int[] {1, 2, 3}));
  }
}
