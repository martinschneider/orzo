package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class TypeVariables<T> {
  // a field and locals declared with a type variable are erased to its bound
  T value;

  TypeVariables(T value) {
    this.value = value;
  }

  T get() {
    T result = value;
    return result;
  }

  static <S extends CharSequence> S longest(List<S> values) {
    S best = values.get(0);
    for (S candidate : values) {
      if (candidate.length() > best.length()) {
        best = candidate;
      }
    }
    return best;
  }

  public static void main(String[] args) {
    TypeVariables<String> box = new TypeVariables<>("boxed");
    Object content = box.get();
    System.out.println(content);
    List<String> words = new ArrayList<>();
    words.add("ab");
    words.add("abcd");
    words.add("abc");
    CharSequence longest = longest(words);
    System.out.println(longest);
  }
}
