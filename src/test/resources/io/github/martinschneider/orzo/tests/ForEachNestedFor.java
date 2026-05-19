package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ForEachNestedFor {
  public static void main(String[] args) {
    List<String> names = new ArrayList<String>();
    names.add("Alice");
    names.add("Bob");
    int total = 0;
    for (String name : names) {
      for (int i = 0; i < name.length(); i++) {
        total = total + 1;
      }
    }
    System.out.println(total);
  }
}
