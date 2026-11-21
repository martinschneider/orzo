package io.github.martinschneider.orzo.tests;

public class QualifiedInstanceof {
  public static void main(String[] args) {
    Object o = new StringBuilder("x");
    if (o instanceof java.lang.StringBuilder) {
      System.out.println("builder");
    }
    System.out.println(o instanceof java.util.List);
  }
}
