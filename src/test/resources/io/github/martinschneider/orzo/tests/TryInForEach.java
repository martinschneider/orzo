package io.github.martinschneider.orzo.tests;

public class TryInForEach {
  public static void main(String[] args) {
    StringBuilder sb = new StringBuilder();
    int[] nums = new int[] {1, 2, 3};
    for (int n : nums) {
      try {
        sb.append(n).append(",");
      } catch (Exception e) {
      }
    }
    System.out.println(sb.toString());
  }
}
