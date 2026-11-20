package io.github.martinschneider.orzo.tests;

public class NegatedLogicalOp {
  public static void main(String[] args) {
    Object o = "s";
    // the cast must not be evaluated (short-circuit) when o isn't an Integer
    if (!(o instanceof Integer && ((Integer) o).equals(5))) {
      System.out.println("not five");
    }
    boolean r = !(o instanceof String || o == null);
    System.out.println(r);
    while (!(o == null || o instanceof Integer)) {
      System.out.println("loop");
      o = null;
    }
  }
}
