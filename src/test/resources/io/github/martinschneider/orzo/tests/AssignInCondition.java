package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class AssignInCondition {
  static int idx = 0;
  static List<String> data = new ArrayList<>();

  static String getNext() {
    if (idx < data.size()) {
      return data.get(idx++);
    }
    return null;
  }

  public static void main(String[] args) {
    data.add("foo");
    data.add("bar");
    data.add("baz");
    String item;
    if ((item = getNext()) != null) {
      System.out.println(item);
    }
    while (idx < data.size()) {
      if ((item = getNext()) != null) {
        System.out.println(item);
      }
    }
  }
}
