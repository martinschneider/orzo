package io.github.martinschneider.kommpeiler.examples;

public class K014_ParallelAssignments {
  public static void main(String[] args) {
    int a = 1;
    int b = 2;
    a,b = b,a; // standard Java does not support this
    System.out.println(a);
    System.out.println(b);
    a,b = a+1,b+1;
    System.out.println(a);
    System.out.println(b);
  }
}
