package io.github.martinschneider.orzo.tests;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GenericMethodInference {
  static int total(List... lists) {
    int n = 0;
    for (List l : lists) {
      n += l.size();
    }
    return n;
  }

  public static void main(String[] args) {
    String[] parts = new String[] {"a", "b", "c"};
    // <T> T[] copyOf(T[], int) returns String[] here, so join(CharSequence, CharSequence...) applies
    System.out.println(String.join(".", Arrays.copyOf(parts, 2)));
    String[] copy = Arrays.copyOf(parts, 1);
    System.out.println(copy[0].toUpperCase());
    System.out.println(Objects.requireNonNull("x").length());
    // varargs parameter of an imported type
    System.out.println(total(Arrays.asList(1, 2), Arrays.asList(3)));
  }
}
