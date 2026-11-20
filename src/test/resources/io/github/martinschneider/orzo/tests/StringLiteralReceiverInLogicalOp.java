package io.github.martinschneider.orzo.tests;

public class StringLiteralReceiverInLogicalOp {
  public static void main(String[] args) {
    boolean b = true;
    String s = "super";
    if (b && "super".equals(s)) {
      System.out.println("a");
    }
    if (!b || "x".length() == 1) {
      System.out.println("b");
    }
    boolean c = b && "super".equals("duper");
    System.out.println(c);
  }
}
