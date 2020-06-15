package io.github.martinschneider.orzo.tests;

public class JavaLangImports {
  public static void main(String[] args) {
    for (int i = 0; i < 32; i++) {
      System.out.println(Integer.bitCount(i));
    }
    System.out.println(Double.parseDouble("13.0") - 5);
    System.out.println(Math.max(Long.parseLong("123"), Integer.parseInt("456")));
    System.out.println(Character.forDigit(7, 10));
  }
}
