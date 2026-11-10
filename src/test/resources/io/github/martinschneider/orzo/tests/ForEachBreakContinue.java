package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ForEachBreakContinue {
  public static void main(String[] args) {
    int[] nums = new int[5];
    nums[0] = 1;
    nums[1] = 2;
    nums[2] = 3;
    nums[3] = 4;
    nums[4] = 5;
    for (int n : nums) {
      if (n == 3) {
        continue;
      }
      if (n == 5) {
        break;
      }
      System.out.println(n);
    }

    List<String> names = new ArrayList<String>();
    names.add("Alice");
    names.add("Bob");
    names.add("Charlie");
    names.add("Dave");
    for (String name : names) {
      if (name.equals("Bob")) {
        continue;
      }
      if (name.equals("Dave")) {
        break;
      }
      System.out.println(name);
    }
  }
}
