package io.github.martinschneider.orzo.tests;

public class NestedTernary {
  public static void main(String[] args) {
    Object o = " x ";
    boolean b = true;
    int x = 10;
    String name = "a";
    name += "." + (o instanceof String ? ((String) o).trim() : o.toString());
    System.out.println(name);
    int n = 1;
    n += (o == null ? 1 : 2);
    System.out.println(n);
    System.out.println(2 * (x + (b ? 1 : 2)) - 1);
    System.out.println((b ? 1 : 2) + (!b ? 3 : 4));
  }
}
