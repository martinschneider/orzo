package io.github.martinschneider.orzo.tests;

import java.util.HashMap;
import java.util.Map;

public enum EnumConstructors {
  RED("r", 1, 2L),
  GREEN("g", 3, 4L),
  BLUE("b");

  private static final Map<String, EnumConstructors> BY_CODE = new HashMap<>();

  static {
    for (EnumConstructors c : values()) {
      BY_CODE.put(c.code, c);
    }
  }

  private final String code;
  private final int a;
  private final long b;
  private int count = 7;

  EnumConstructors(String code, int a, long b) {
    this.code = code;
    this.a = a * 10;
    this.b = b;
  }

  EnumConstructors(String code) {
    this(code, 0, 0L);
  }

  public static void main(String[] args) {
    System.out.println(GREEN.code + " " + GREEN.a + " " + GREEN.b + " " + RED.count + " " + RED.ordinal() + " " + GREEN.name());
    System.out.println(BY_CODE.get("b") + " " + BLUE.a + " " + values().length);
  }
}
