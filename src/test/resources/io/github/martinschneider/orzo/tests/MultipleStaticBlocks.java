package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class MultipleStaticBlocks {
  private static final List<String> FIRST = new ArrayList<>();

  static {
    FIRST.add("a");
  }

  private static final List<String> SECOND = new ArrayList<>();

  static {
    SECOND.add("b");
    SECOND.add("c");
  }

  static {}

  public static void main(String[] args) {
    System.out.println(FIRST.size());
    System.out.println(SECOND.size());
  }
}
