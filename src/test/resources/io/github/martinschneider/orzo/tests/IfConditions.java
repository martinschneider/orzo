package io.github.martinschneider.orzo.tests;

public class IfConditions {
  public static void main(String[] args) {
    int a = 1;
    int b = 1;
    int c = 0;
    if (a == b) {
      b = 2;
    }
    if (b > 0) {
      c = 3;
    }
    System.out.println(a);
    System.out.println(b);
    System.out.println(c);
  }
}
