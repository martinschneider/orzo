package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

public class LambdaBlocks {
  static int total = 0;

  // lambdas with a block body, and for void functional interfaces
  public static void main(String[] args) {
    List<String> log = new ArrayList<>();
    String prefix = "> ";
    Consumer<String> logger = s -> log.add(prefix + s);
    logger.accept("one");
    Function<Integer, String> describe =
        n -> {
          if (n < 0) {
            return "negative";
          }
          String result = "";
          for (int i = 0; i < n; i++) {
            result += "*";
          }
          return result;
        };
    IntBinaryOperator max =
        (a, b) -> {
          int larger = a;
          if (b > a) {
            larger = b;
          }
          return larger;
        };
    Runnable count =
        () -> {
          total++;
          log.add(prefix + "counted " + total);
        };
    count.run();
    count.run();
    System.out.println(describe.apply(3) + " " + describe.apply(-1) + " " + max.applyAsInt(4, 9));
    System.out.println(log);
  }
}
