package io.github.martinschneider.orzo.tests;

public class StringConcat {
  public static void main(String[] args) {
    String a = "hello";
    String b = "world";
    System.out.println(a + " " + b);
    int n = 42;
    System.out.println("n=" + n);
    System.out.println("sum=" + (n + 1));
    boolean flag = true;
    System.out.println("flag=" + flag);
    double d = 3.14;
    System.out.println("pi=" + d);
    System.out.println("java." + "lang");
    System.out.println("x" + 1 + 2);
  }
}
