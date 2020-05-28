package io.github.martinschneider.kommpeiler.examples;

public class PiLeibniz {
  public static void main(String[] args) {
    // Calculate pi using the Leibniz series
    double d = 1;
    for (int i = 1; i < 1000000; i++) {
      if (i % 2 == 0) {
        d += 1 / (2 * i + 1);
      } else {
        d -= 1 / (2 * i + 1);
      }
    }
    System.out.println(4 * d);
  }
}
