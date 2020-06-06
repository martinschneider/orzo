package io.github.martinschneider.orzo.tests;

public class ParallelAssignmentsFloat {
  public static void main(String[] args) {
    float a = 1;
    float b = 2;
    a,b = b+1,a+1;
    System.out.println(a);
    System.out.println(b);
  }
}
