package io.github.martinschneider.orzo.tests;

import static io.github.martinschneider.orzo.examples.MathUtils.pi;
import static io.github.martinschneider.orzo.examples.MathUtils.times;
import io.github.martinschneider.orzo.examples.MathUtils;

public class Circles {
  public static void main(String[] args) {
    int r = 10;
    System.out.println(r);
    System.out.println(MathUtils.times(MathUtils.pi(), MathUtils.times(r, r)));
    System.out.println(MathUtils.times(MathUtils.pi(), MathUtils.times(2, r)));
    System.out.println(times(pi(), times(r, r)));
    System.out.println(times(pi(), times(2, r)));
  }
}
