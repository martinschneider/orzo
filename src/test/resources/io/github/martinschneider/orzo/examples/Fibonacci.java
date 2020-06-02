package io.github.martinschneider.orzo.examples;

public class Fibonacci {
  public static void main(String[] args) {
    int f1 = 0;
    int f2 = 1;
    int f = f1 + f2;
    while (f < 100) {
      System.out.println(f);
      f1 = f2;
      f2 = f;
      f = f1 + f2;
    }
  }
}
