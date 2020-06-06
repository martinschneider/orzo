package io.github.martinschneider.orzo.tests;

public class ParallelAssignmentsMultiple {
  public static void main(String[] args) {
    int a = 1;
    int b = 2;
    a,b = b+1,a+1;
    System.out.println(a);
    System.out.println(b);

    double c = 1.0;
    double d = 2.0;
    c,d = d-1,c-1;
    System.out.println(c);
    System.out.println(d);

    long e = 100L;
    long f = 200L;
    long g = 300L;
    e,f,g=2 * e, 3 * f, 4 * g;
    System.out.println(e); // 200
    System.out.println(f); // 600
    System.out.println(g); // 900
  }
}
