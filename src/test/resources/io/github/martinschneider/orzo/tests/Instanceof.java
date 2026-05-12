package io.github.martinschneider.orzo.tests;

public class Instanceof {
  public static void main(String[] args) {
    Object s = "hello";
    Object n = Integer.valueOf(42);
    if (s instanceof String) {
      System.out.println("yes");
    }
    if (n instanceof String) {
      System.out.println("no");
    } else {
      System.out.println("not a string");
    }
  }
}
