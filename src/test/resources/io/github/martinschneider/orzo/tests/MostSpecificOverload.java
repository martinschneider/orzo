package io.github.martinschneider.orzo.tests;

import java.util.stream.Stream;

public class MostSpecificOverload {
  static long count(String... values) {
    // Stream.of(T...) is more specific than Stream.of(T) for an array argument
    return Stream.of(values).count();
  }

  public static void main(String[] args) {
    System.out.println(count("a", "b", "c"));
    StringBuilder sb = new StringBuilder();
    Object o = "obj";
    sb.append(o).append("s").append('c').append(1);
    System.out.println(sb);
  }
}
