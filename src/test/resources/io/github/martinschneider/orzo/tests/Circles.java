package io.github.martinschneider.orzo.tests;

import static io.github.martinschneider.orzo.examples.MathUtils.pi;
import io.github.martinschneider.orzo.examples.MathUtils;

public class Circles {
  public static void main(String[] args) {
    int r = 10;
    System.out.println(r);
    System.out.println(MathUtils.pi() * (r ** 2));
    System.out.println(MathUtils.pi() * 2 * r);
    System.out.println(pi() * (r ** 2));
    System.out.println(pi() * 2 * r);
  }
}
