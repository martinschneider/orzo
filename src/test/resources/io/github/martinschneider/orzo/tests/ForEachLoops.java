package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ForEachLoops {
  public static void main(String[] args) {
    List<String> names = new ArrayList<String>();
    names.add("Alice");
    names.add("Bob");
    names.add("Charlie");
    for (String name : names) {
      System.out.println(name);
    }
  }
}
