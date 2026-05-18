package io.github.martinschneider.orzo.tests;

public class CastMethodChain {
  public static void main(String[] args) {
    Object obj = "hello";
    System.out.println(((String) obj).length());
    System.out.println(((String) obj).toUpperCase());
  }
}
