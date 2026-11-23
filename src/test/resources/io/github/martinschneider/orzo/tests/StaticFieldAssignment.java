package io.github.martinschneider.orzo.tests;

public class StaticFieldAssignment {
  static int total = 0;

  // assignments to a static field qualified by its class name
  public static void main(String[] args) {
    StaticFieldAssignment.total = 3;
    StaticFieldAssignment.total++;
    ++StaticFieldAssignment.total;
    StaticFieldAssignment.total += 5;
    System.out.println(StaticFieldAssignment.total + " " + total);
  }
}
