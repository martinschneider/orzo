package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ForEachNestedBranch {
  public static void main(String[] args) {
    List<String> names = new ArrayList<String>();
    names.add("Alice");
    names.add("Bob");
    names.add("Charlie");
    for (String name : names) {
      if (name.length() > 3) {
        System.out.println(name);
      }
    }
  }
}
