package io.github.martinschneider.orzo.tests;

import java.util.function.Consumer;

public class BridgeMethodsWithParam implements Consumer<String> {
  public void accept(String s) {
    System.out.println(s);
  }

  public static void main(String[] args) {
    System.out.println("ok");
  }
}
