package io.github.martinschneider.orzo.tests;

public class DoubleParen {
  public static void main(String[] args) {
    Object obj = "hello";
    String s = ((String) obj);
    System.out.println(s);
    Object obj2 = "world";
    System.out.println(((String) obj2));
  }
}
