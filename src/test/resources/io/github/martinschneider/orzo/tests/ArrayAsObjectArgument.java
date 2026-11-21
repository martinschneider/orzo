package io.github.martinschneider.orzo.tests;

import static java.util.Map.entry;
import java.util.Map;
public class ArrayAsObjectArgument {
  public static void main(String[] args) {
    Map<String, byte[]> m = Map.ofEntries(entry("a", new byte[] {1, 2}), entry("b", new byte[] {3}));
    System.out.println(m.get("a").length);
  }
}
