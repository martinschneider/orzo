package io.github.martinschneider.orzo.tests;

public class ChainedFieldAssignment {
  public static void main(String[] args) {
    FieldHolder h = new FieldHolder();
    h.x = 42;
    System.out.println(h.x);
    h.label = "hello";
    System.out.println(h.label);
  }
}
