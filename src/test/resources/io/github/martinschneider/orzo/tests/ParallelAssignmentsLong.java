package io.github.martinschneider.orzo.tests;

public class ParallelAssignmentsLong {
  public static void main(String[] args) {
    long a = 1;
    long b = 2;
    a,b = b+1,a+1;
    System.out.println(a);
    System.out.println(b);
  }
}
