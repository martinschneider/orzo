package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class LabeledLoops {
  public static void main(String[] args) {
    // continue an outer for loop from an inner for-each
    List<String> words = new ArrayList<>();
    words.add("ab");
    words.add("cd");
    outer:
    for (int i = 0; i < 3; i++) {
      for (String w : words) {
        if (w.equals("cd") && i == 1) {
          continue outer;
        }
        System.out.println(i + w);
      }
      System.out.println("end " + i);
    }
    // break an outer while loop from an inner for loop
    int n = 0;
    search:
    while (n < 100) {
      for (int j = 0; j < 10; j++) {
        if (n * j == 12) {
          break search;
        }
      }
      n++;
    }
    System.out.println("n=" + n);
    // break out of a labeled block from an inner loop
    int found = -1;
    block:
    {
      int k = 0;
      while (true) {
        if (k * k > 20) {
          found = k;
          break block;
        }
        k++;
      }
    }
    System.out.println("found=" + found);
  }
}
