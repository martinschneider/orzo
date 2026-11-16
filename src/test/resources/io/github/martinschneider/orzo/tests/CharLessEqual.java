package io.github.martinschneider.orzo.tests;

public class CharLessEqual {
  private static boolean isHexDigit(char c) {
    return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  public static void main(String[] args) {
    System.out.println(isHexDigit('3'));
    System.out.println(isHexDigit('a'));
    System.out.println(isHexDigit('f'));
    System.out.println(isHexDigit('A'));
    System.out.println(isHexDigit('F'));
    System.out.println(isHexDigit('g'));
    System.out.println(isHexDigit('G'));
  }
}
