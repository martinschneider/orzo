package io.github.martinschneider.orzo.examples;

public class ParallelAssignmentsShort {
  public static void main(String[] args) {
    short a = 1;
    short b = 2;
    a,b = b+1,a+1;
    System.out.println(a);
    System.out.println(b);
  }
}
