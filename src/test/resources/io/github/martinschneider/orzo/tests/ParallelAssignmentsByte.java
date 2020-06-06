package io.github.martinschneider.orzo.tests;

public class ParallelAssignmentsByte {
  public static void main(String[] args) {
    byte a = 1;
    byte b = 2;
    a,b = b+1,a+1;
    System.out.println(a);
    System.out.println(b);
  }
}
