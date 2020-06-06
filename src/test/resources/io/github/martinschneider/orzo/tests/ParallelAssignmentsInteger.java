package io.github.martinschneider.orzo.tests;

public class ParallelAssignmentsInteger {
  public static void main(String[] args) {
    int a = 1;
    int b = 2;
    a,b = b+1,a+1;
    System.out.println(a);
    System.out.println(b);
  }
}
