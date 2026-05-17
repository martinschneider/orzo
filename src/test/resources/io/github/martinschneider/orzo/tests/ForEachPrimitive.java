package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ForEachPrimitive {
  public static void main(String[] args) {
    List<Integer> ints = new ArrayList<Integer>();
    ints.add(1);
    ints.add(2);
    ints.add(3);
    int sum = 0;
    for (int n : ints) {
      sum = sum + n;
    }
    System.out.println(sum);
  }
}
