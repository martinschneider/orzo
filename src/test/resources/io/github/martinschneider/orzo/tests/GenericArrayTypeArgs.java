package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;

public class GenericArrayTypeArgs {
  static final Map<String, Map<String, byte[]>> OPS = new HashMap<>();
  static final Map<String, String[]> NAMES = new HashMap<>();

  public static void main(String[] args) {
    Map<String, byte[]> inner = new HashMap<>();
    // an array literal as an Object argument must not be boxed like a scalar
    inner.put("x", new byte[] {1, 2, 3});
    OPS.put("a", inner);
    NAMES.put("n", new String[] {"p", "q"});
    byte[] b = OPS.getOrDefault("a", new HashMap<>()).getOrDefault("x", new byte[0]);
    System.out.println(b.length);
    // a call chain followed by a field: the middle call must not be dropped
    System.out.println(OPS.get("a").get("x").length);
    System.out.println(NAMES.get("n")[1].toUpperCase());
    byte[] c = inner.get("x");
    System.out.println(c[2]);
  }
}
