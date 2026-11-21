package io.github.martinschneider.orzo.tests;

public class PrimitiveFirstConcat {
  public static void main(String[] args) {
    String t = "a";
    int i = 1;
    int j = 2;
    char c = 'y';
    long l = 5;
    double d = 1.5;
    System.out.println(i + t);
    t = '[' + t;
    System.out.println(t);
    System.out.println(c + t + c);
    System.out.println(i + j + t);
    System.out.println(t + i * 2 + i);
    String u = l + t;
    System.out.println(u);
    System.out.println(d + "x" + d);
  }
}
