package io.github.martinschneider.orzo.tests;

public class Booleans {
  public static void main(String[] args) {
    boolean a = true;
    boolean b = false;
    System.out.println(a);
    System.out.println(b);
    System.out.println(true);
    System.out.println(false);
    System.out.println(!a);
    System.out.println(!b);
    System.out.println(a || b && !(a && b)); // true
  }
}
