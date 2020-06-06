package io.github.martinschneider.orzo.tests;

public class ParallelAssignmentsDouble {
  public static void main(String[] args) {
    double a = 1;
    double b = 2;
    a,b = b+1,a+1;
    System.out.println(a);
    System.out.println(b);
  }
}
