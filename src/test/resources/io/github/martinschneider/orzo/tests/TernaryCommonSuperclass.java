package io.github.martinschneider.orzo.tests;

public class TernaryCommonSuperclass {
  public static void main(String[] args) {
    boolean b = System.nanoTime() < 0; // false, but not a compile-time constant
    StringBuilder sb = new StringBuilder("sb");
    String s = "str";
    // the two branches have different types: the result is a CharSequence
    CharSequence cs = b ? sb : s;
    System.out.println(cs.length());
    Object o = !b ? Integer.valueOf(1) : Long.valueOf(2L);
    System.out.println(o);
  }
}
