package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ForEachArrayElements {
  public static void main(String[] args) {
    List<int[]> pairs = new ArrayList<>();
    pairs.add(new int[] {1, 2});
    pairs.add(new int[] {3, 4});
    int sum = 0;
    for (int[] pair : pairs) {
      sum += pair[0] * pair[1];
    }
    System.out.println(sum);
    List<String[]> rows = new ArrayList<>();
    rows.add(new String[] {"a", "b"});
    for (String[] row : rows) {
      System.out.println(row[1] + row.length);
    }
  }
}
