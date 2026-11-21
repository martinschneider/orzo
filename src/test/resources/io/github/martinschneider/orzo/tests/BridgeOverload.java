package io.github.martinschneider.orzo.tests;

import java.util.function.ObjIntConsumer;

public class BridgeOverload implements ObjIntConsumer<String>, Comparable<BridgeOverload> {
  int total;

  // same name and arity as the bridged accept(T, int): must not be the bridge's target
  public void accept(String s, long v) {
    total = -100;
  }

  public void accept(String s, int v) {
    total += s.length() + v;
  }

  public int compareTo(BridgeOverload other) {
    return total - other.total;
  }

  public static void main(String[] args) {
    BridgeOverload b = new BridgeOverload();
    ObjIntConsumer<String> c = b;
    c.accept("ab", 3);
    System.out.println(b.total);
    // a java.lang interface
    Comparable<BridgeOverload> cmp = b;
    System.out.println(cmp.compareTo(new BridgeOverload()));
  }
}
