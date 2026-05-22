package io.github.martinschneider.orzo.tests;

public class InstanceofAnd {
  public static boolean check(byte type, Object key) {
    byte CONSTANT_STRING = 8;
    return type == CONSTANT_STRING && key instanceof String;
  }

  public static void main(String[] args) {
    System.out.println(check((byte) 8, "hello"));
    System.out.println(check((byte) 8, Integer.valueOf(42)));
    System.out.println(check((byte) 5, "hello"));
    System.out.println(check((byte) 5, Integer.valueOf(42)));
  }
}
