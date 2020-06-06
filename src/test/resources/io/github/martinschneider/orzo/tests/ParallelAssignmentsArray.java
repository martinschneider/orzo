package io.github.martinschneider.orzo.tests;

public class ParallelAssignmentsArray {
  public static void main(String[] args) {
    int[] a = new int[] {1,2};
    a[1], a[0] = a[0], a[1];
    System.out.println(a[0]); // 2
    System.out.println(a[1]); // 1

    int[]b = new int[] {3,4};
    a, b = b, a;
    System.out.println(a[0]); // 3
    System.out.println(a[1]); // 4
    System.out.println(b[0]); // 2
    System.out.println(b[1]); // 1

    a[0],b = a[1],a;
    System.out.println(a[0]); // 4
    System.out.println(a[1]); // 4
    System.out.println(b[0]); // 4
    System.out.println(b[1]); // 4
  }
}
