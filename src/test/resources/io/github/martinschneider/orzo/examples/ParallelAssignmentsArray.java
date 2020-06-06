package io.github.martinschneider.orzo.examples;

public class ParallelAssignmentsArray {
  public static void main(String[] args) {
    int[] a = new int[] {1,2};
    int[] b = new int[] {3,4};
    a[1],a[2] = a[2],a[1];
    System.out.println(a[0]);
    System.out.println(a[1]);
    System.out.println(b[0]);
    System.out.println(b[1]);
    //a[1],b = a[2],a;
    System.out.println(a[0]);
    System.out.println(a[1]);
    System.out.println(b[0]);
    System.out.println(b[1]);
  }
}
