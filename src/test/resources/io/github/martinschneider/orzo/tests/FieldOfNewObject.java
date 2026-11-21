package io.github.martinschneider.orzo.tests;

public class FieldOfNewObject {
  static int base = 5;
  // an instance field initializer reading a static field
  int value = base + 1;
  String name = "n";

  public static void main(String[] args) {
    // a field read directly on a constructor result
    System.out.println(new FieldOfNewObject().value);
    System.out.println(new FieldOfNewObject().name.length());
  }
}
