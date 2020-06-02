package io.github.martinschneider.orzo.examples;

public class ParallelAssignments {
  public static void main(String[] args) {
    int a = 1;
    int b = 2;
    a,b = b+1,a+1; // standard Java does not support this
    System.out.println(a);
    System.out.println(b);
  }
}
