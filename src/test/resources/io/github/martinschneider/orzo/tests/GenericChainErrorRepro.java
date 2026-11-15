package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;

public class GenericChainErrorRepro {
  Map<String, Object> reg = new HashMap<>();

  public void length() {
    System.out.println("wrong: enclosing-class method should never be called here");
  }

  public static void main(String[] args) {
    GenericChainErrorRepro r = new GenericChainErrorRepro();
    r.reg.put("k", "hello");
    System.out.println(r.reg.get("k").length());
  }
}
