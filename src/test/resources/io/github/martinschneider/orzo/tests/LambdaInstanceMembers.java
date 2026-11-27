package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class LambdaInstanceMembers {
  int base = 10;
  List<String> log = new ArrayList<>();

  private static void show(String s) {
    System.out.println("> " + s);
  }

  int twice(int x) {
    return 2 * x;
  }

  // lambdas using the fields and methods of the enclosing instance (and "this")
  void run() {
    Function<Integer, Integer> plusBase = x -> x + base;
    Function<Integer, Integer> twiceBase = x -> twice(x) + this.base;
    Runnable logger = () -> log.add("ran " + plusBase.apply(1));
    logger.run();
    Supplier<LambdaInstanceMembers> self = () -> this;
    System.out.println(plusBase.apply(1) + " " + twiceBase.apply(2) + " " + log);
    System.out.println(self.get() == this);
    // (a reference to a private static method)
    log.forEach(LambdaInstanceMembers::show);
    List.of("x", "y").forEach(LambdaInstanceMembers::show); // (an erased element type)
    Function<Integer, Integer> doubler = this::twice;
    System.out.println(doubler.apply(21));
    // ("this" in operations: indexes, object creations, conditionals, casts and instanceof)
    Supplier<String> mixed =
        () ->
            this.values[1]
                + " "
                + new Wrapper(this).owner.base
                + " "
                + (this.base > 5 ? this.base : -this.base)
                + " "
                + ((Object) this).hashCode() / Math.max(1, this.hashCode())
                + " "
                + (this instanceof Object);
    System.out.println(mixed.get());
  }

  int[] values = {4, 5};

  static class Wrapper {
    LambdaInstanceMembers owner;

    Wrapper(LambdaInstanceMembers owner) {
      this.owner = owner;
    }
  }

  class Inner {
    int own = 3;

    int sum() {
      Supplier<Integer> sum = () -> own + base;
      return sum.get();
    }
  }

  int inner() {
    return new Inner().sum();
  }

  public static void main(String[] args) {
    LambdaInstanceMembers outer = new LambdaInstanceMembers();
    outer.run();
    System.out.println(outer.inner());
  }
}
