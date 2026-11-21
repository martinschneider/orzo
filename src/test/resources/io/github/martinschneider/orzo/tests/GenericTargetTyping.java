package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class GenericTargetTyping {
  int idx;
  String name;
  GenericTargetTyping(int idx, String name) {
    this.idx = idx;
    this.name = name;
  }
  static final byte BASE = 10;
  static boolean is(byte op, int slot) {
    return op - BASE == slot;
  }
  public static void main(String[] args) {
    List<GenericTargetTyping> l = new ArrayList<>();
    l.add(new GenericTargetTyping(3, "c"));
    l.add(new GenericTargetTyping(1, "a"));
    l.sort(Comparator.comparingInt(v -> v.idx));
    System.out.println(l.get(0).name + l.get(1).name + " " + is((byte) 12, 2) + " " + is((byte) 12, 3));
  }
}
