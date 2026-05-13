package io.github.martinschneider.orzo.tests;

import java.util.function.Supplier;

public class BridgeMethods implements Supplier<String> {
  public String get() {
    return "bridge";
  }

  public static void main(String[] args) {
    System.out.println(new BridgeMethods().get());
  }
}
