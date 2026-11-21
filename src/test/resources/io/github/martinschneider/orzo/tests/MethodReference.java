package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
public class MethodReference {
  public static void main(String[] args) {
    Map<Integer, Integer> m = new HashMap<>();
    m.merge(1, 5, Math::min);
    m.merge(1, 3, Math::min);
    m.merge(1, 9, Math::min);
    // Math.min(int, int) is the most specific overload for Integer arguments
    m.merge(2, 4, (a, b) -> Math.min(a, b));
    m.merge(2, 7, (a, b) -> Math.min(a, b));
    System.out.println(m.get(2));
    System.out.println(m.get(1));
    List<String> l = List.of("ab", "c");
    System.out.println(l.stream().map(String::length).collect(Collectors.toList()));
  }
}
