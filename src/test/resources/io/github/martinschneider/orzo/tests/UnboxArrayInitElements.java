package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;
public class UnboxArrayInitElements {
  public static void main(String[] args) {
    Map<String, Byte> m = new HashMap<>();
    m.put("a", (byte) 7);
    Integer boxed = 9;
    byte[] b = new byte[] {m.get("a"), 3};
    int[] i = {boxed, m.get("a")};
    System.out.println(b[0] + b[1] + " " + (i[0] + i[1]));
  }
}
