package io.github.martinschneider.orzo.tests;

public class NullCheckAnd {
  public static void main(String[] args) {
    String s = "hello";
    boolean a = s != null && s.equals("hello");
    boolean b = s != null && s.equals("world");
    String n = null;
    boolean c = n != null && n.equals("hello");
    System.out.println(a);
    System.out.println(b);
    System.out.println(c);
    System.out.println(a ^ b);
    System.out.println(a ^ c);
  }
}
