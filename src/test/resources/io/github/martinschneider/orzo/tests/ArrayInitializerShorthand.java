package io.github.martinschneider.orzo.tests;

public class ArrayInitializerShorthand {
  static int[] counters = {0, 5};

  public static void main(String[] args) {
    int[] a = {1, 2, 3};
    String[] s = {"x", "y"};
    int[] empty = {};
    System.out.println(a.length + " " + a[2] + " " + s[1] + " " + empty.length);
    counters[0]++;
    System.out.println(counters[0] + counters[1]);
  }
}
