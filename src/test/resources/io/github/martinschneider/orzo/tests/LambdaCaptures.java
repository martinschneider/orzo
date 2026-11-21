package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaCaptures {
  static String decorate(String s, String prefix) {
    return prefix + s;
  }

  static String joinAll(List<String> items, String prefix, int[] counts) {
    // captures a parameter, an array and calls a static method of the enclosing class
    return items.stream()
        .map(x -> decorate(x, prefix) + counts[0])
        .collect(Collectors.joining(","));
  }

  static Object concat(String a, String b) {
    return a + b; // a String concatenation even though the method returns Object
  }

  static Object boxed() {
    return 41 + 1;
  }

  public static void main(String[] args) {
    System.out.println(concat("x", "y"));
    System.out.println(boxed());
    List<String> items = new ArrayList<>();
    items.add("a");
    items.add("b");
    System.out.println(joinAll(items, "#", new int[] {7}));
    String suffix = "!";
    System.out.println(items.stream().map(x -> x + suffix).collect(Collectors.toList()));
  }
}
