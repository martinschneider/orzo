package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class WhileAssign {
  static int idx = 0;
  static List<String> data = new ArrayList<>();

  static String getNext() {
    if (idx < data.size()) {
      return data.get(idx++);
    }
    return null;
  }

  public static void main(String[] args) {
    data.add("one");
    data.add("two");
    data.add("three");
    String item;
    while (idx < data.size()) {
      String next = getNext();
      if ((item = next) != null) {
        System.out.println(item);
      }
    }
  }
}
