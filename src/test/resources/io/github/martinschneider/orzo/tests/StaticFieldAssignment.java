package io.github.martinschneider.orzo.tests;

public class StaticFieldAssignment {
  static int total = 0;

  static class Nested {
    // (a static field of the enclosing class)
    static void add() {
      total++;
      total += 10;
    }
  }

  // assignments to a static field qualified by its class name
  public static void main(String[] args) {
    StaticFieldAssignment.total = 3;
    StaticFieldAssignment.total++;
    ++StaticFieldAssignment.total;
    StaticFieldAssignment.total += 5;
    System.out.println(StaticFieldAssignment.total + " " + total);
    Nested.add();
    System.out.println(total);
  }
}
