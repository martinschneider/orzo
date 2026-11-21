package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class QualifiedTypeBound {
  // a method type parameter with a qualified bound (erased to it)
  static <T extends java.lang.CharSequence> T first(List<T> values) {
    return values.get(0);
  }

  public static void main(String[] args) throws Exception {
    List<String> words = new ArrayList<>();
    words.add("ab");
    CharSequence first = first(words);
    System.out.println(first);
    System.out.println(
        QualifiedTypeBound.class.getDeclaredMethod("first", List.class).getReturnType().getName());
  }
}
