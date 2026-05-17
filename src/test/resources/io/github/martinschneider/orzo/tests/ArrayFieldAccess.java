package io.github.martinschneider.orzo.tests;

public class ArrayFieldAccess {
  public static void main(String[] args) {
    int[] nums = new int[]{10, 20, 30};
    ArrayFieldHolder h = new ArrayFieldHolder(nums);
    System.out.println(h.data.length);
    System.out.println(h.data[0]);
    System.out.println(h.data[2]);
  }
}
