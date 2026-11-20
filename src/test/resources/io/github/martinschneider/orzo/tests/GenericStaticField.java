package io.github.martinschneider.orzo.tests;

import static io.github.martinschneider.orzo.tests.GenericStaticFieldHolder.REGISTRY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericStaticField {
  private static final Map<String, List<String>> LOCAL = new HashMap<>();

  public static void main(String[] args) {
    REGISTRY.put("k", new ArrayList<>());
    REGISTRY.get("k").add("abc");
    System.out.println(REGISTRY.get("k").get(0).length());
    LOCAL.put("x", new ArrayList<>());
    LOCAL.get("x").add("hello");
    System.out.println(LOCAL.get("x").get(0).toUpperCase());
  }
}
