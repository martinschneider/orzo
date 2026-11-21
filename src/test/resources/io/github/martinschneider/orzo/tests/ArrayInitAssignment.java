package io.github.martinschneider.orzo.tests;

public class ArrayInitAssignment {
  public static void main(String[] args) {
    byte[] op;
    byte b = 5;
    op = new byte[] {b, 6};
    String[] s;
    s = new String[] {"x"};
    System.out.println(op[0] + op[1] + " " + s[0]);
  }
}
