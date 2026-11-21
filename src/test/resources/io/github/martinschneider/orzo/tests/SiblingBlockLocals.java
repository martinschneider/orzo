package io.github.martinschneider.orzo.tests;

public class SiblingBlockLocals {
  public static void main(String[] args) {
    for (int i = 0; i < 2; i++) {
      try {
        String value = "s" + i;
        System.out.println(value.length());
      } catch (RuntimeException e) {
        System.out.println("error");
      }
      for (int j = 0; j < 1; j++) {
        StringBuilder value = new StringBuilder("sb");
        System.out.println(value.reverse());
      }
    }
  }
}
