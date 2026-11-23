package io.github.martinschneider.orzo.tests;

public final class ModifierOrder {
  // modifiers in any order, and modifiers that only set access flags
  static public int counter = 1;
  final static String NAME = "order";
  transient int cache = 2;
  volatile boolean flag = true;

  static public class Inner {
    final public static int VALUE = 3;
  }

  synchronized public static int next() {
    return ++counter;
  }

  final private int twice(int x) {
    return 2 * x;
  }

  public static void main(String[] args) {
    ModifierOrder o = new ModifierOrder();
    System.out.println(NAME + " " + next() + " " + Inner.VALUE);
    System.out.println(o.twice(o.cache) + " " + o.flag);
  }
}
