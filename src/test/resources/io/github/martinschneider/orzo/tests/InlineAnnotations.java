package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class InlineAnnotations {
  // annotations on the same line as the code they annotate, and annotations with arguments (they
  // are ignored)
  @Deprecated int field = 3;

  @SuppressWarnings({"unchecked", "rawtypes"})
  static List<String> names() {
    List<String> names = new ArrayList<>();
    names.add("(a)");
    return names;
  }

  static int twice(@Deprecated int x) {
    return 2 * x;
  }

  @Override public String toString() { return "toString"; }

  public static void main(String[] args) {
    System.out.println(new InlineAnnotations().field);
    System.out.println(names());
    System.out.println(twice(21));
    System.out.println(new InlineAnnotations());
  }
}
