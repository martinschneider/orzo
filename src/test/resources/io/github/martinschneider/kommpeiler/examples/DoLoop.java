package io.github.martinschneider.kommpeiler.examples;

public class DoLoop {
  public static void main(String[] args) {
    int n = -5;
    do {
      System.out.println(n);
      n = n + 1;
    } while (n <= 5);
  }
}
