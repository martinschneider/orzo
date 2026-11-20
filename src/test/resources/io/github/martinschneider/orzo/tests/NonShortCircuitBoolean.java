package io.github.martinschneider.orzo.tests;

public class NonShortCircuitBoolean {
  public static void main(String[] args) {
    String a = "x";
    String b = null;
    if (a != null & b != null) {
      System.out.println("both");
    } else {
      System.out.println("not both");
    }
    boolean either = a != null | b != null;
    System.out.println(either);
    int i = 3;
    if (i > 1 & i < 5) {
      System.out.println("in range");
    }
    boolean t = true;
    System.out.println(t ^ b == null);
    System.out.println(6 & 3);
  }
}
