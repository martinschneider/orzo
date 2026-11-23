package io.github.martinschneider.orzo.tests;

import java.util.*;

public class GenericMethods {
  // generic methods: bounded type parameters, explicit type arguments
  static <T> T first(List<T> list) {
    return list.get(0);
  }

  static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;
  }

  static <K, V extends Number & Comparable<V>> String entry(K key, V value) {
    return key + "=" + value;
  }

  public static void main(String[] args) {
    String s = first(List.of("a", "b"));
    System.out.println(s + " " + max(3, 7) + " " + max("x", "y"));
    System.out.println(GenericMethods.<String>first(List.of("z")) + " " + entry("k", 2));
  }
}
