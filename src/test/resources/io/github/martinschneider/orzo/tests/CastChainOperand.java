package io.github.martinschneider.orzo.tests;

public class CastChainOperand {
  public static void main(String[] args) {
    Object o = "hello";
    boolean b = true;
    if (b && ((String) o).startsWith("he")) {
      System.out.println("starts");
    }
    if (!(o instanceof String && ((String) o).isEmpty())) {
      System.out.println("not empty");
    }
    System.out.println(1 + ((String) o).length() * 2);
  }
}
