package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

public class TypedLambdaParameters {
  static int twice(final int x) {
    return 2 * x;
  }

  // explicitly typed (and final) lambda and method parameters
  public static void main(final String[] args) {
    Function<Integer, Integer> plusOne = (Integer x) -> x + 1;
    BiFunction<String, Integer, String> repeat = (final String s, Integer n) -> s.repeat(n);
    Comparator<String> byLength = (String a, String b) -> a.length() - b.length();
    List<String> words = new ArrayList<>(List.of("ccc", "a", "bb"));
    words.sort((String a, String b) -> b.compareTo(a));
    IntBinaryOperator times = (int a, int b) -> a * b;
    System.out.println(plusOne.apply(1) + " " + repeat.apply("ab", 2) + " " + byLength.compare("aa", "b"));
    System.out.println(words + " " + times.applyAsInt(3, 4) + " " + twice(5));
  }
}
