package io.github.martinschneider.orzo.tests;

public class Ternary {
  public static void main(String[] args) {
    int a = 5;
    int b = 3;
    int max = (a > b ? a : b);
    System.out.println(max);
    int x = 0;
    int y = (x == 0 ? 1 : 2);
    System.out.println(y);
    int z = (a < b ? a : b);
    System.out.println(z);
    int w = (a != b ? 10 : 20);
    System.out.println(w);
  }
}
