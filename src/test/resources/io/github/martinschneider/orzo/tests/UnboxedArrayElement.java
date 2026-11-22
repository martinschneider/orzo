package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnboxedArrayElement {
  public static void main(String[] args) {
    // a boxed value stored into an element of a primitive array
    Map<String, Byte> bytes = Map.of("x", (byte) 5);
    byte[] b = new byte[2];
    b[1] = bytes.get("x");
    List<Integer> ints = new ArrayList<>();
    ints.add(42);
    int[] i = new int[1];
    i[0] = ints.get(0);
    System.out.println(b[1] + " " + i[0]);
  }
}
