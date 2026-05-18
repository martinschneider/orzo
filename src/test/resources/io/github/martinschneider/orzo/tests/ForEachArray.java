package io.github.martinschneider.orzo.tests;

public class ForEachArray {
  static int[] nums = new int[3];

  public static void main(String[] args) {
    nums[0] = 1;
    nums[1] = 2;
    nums[2] = 3;
    int sum = 0;
    for (int n : nums) {
      sum = sum + n;
    }
    System.out.println(sum);
  }
}
