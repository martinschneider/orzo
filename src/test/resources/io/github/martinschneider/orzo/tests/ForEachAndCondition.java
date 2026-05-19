package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ForEachAndCondition {
  public static void main(String[] args) {
    List<String> words = new ArrayList<String>();
    words.add("hello");
    words.add("hi");
    words.add("world");
    int limit = 3;
    for (String word : words) {
      int count = 0;
      for (int i = 0; i < limit && i < word.length(); i++) {
        count = count + 1;
      }
      System.out.println(count);
    }
  }
}
