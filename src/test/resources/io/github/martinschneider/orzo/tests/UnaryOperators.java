package io.github.martinschneider.orzo.tests;

public class UnaryOperators {
  public static void main(String[] args) {
    int x = 5;
    long l = 6L;
    int y = +x;
    int z = 3 & ~x;
    // a unary minus in the middle of an expression (the following statement used to be
    // swallowed into the expression)
    int w = 3 & -x;
    System.out.println(~x);
    System.out.println(~l);
    System.out.println(y + " " + z + " " + w);
    System.out.println(~(x + 1));
    System.out.println(~-x);
    System.out.println(x + +y);
  }
}
