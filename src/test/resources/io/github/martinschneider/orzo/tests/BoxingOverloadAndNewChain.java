package io.github.martinschneider.orzo.tests;

public class BoxingOverloadAndNewChain {
  static String box(Integer i) {
    return "Integer " + i;
  }

  static String box(String s) {
    return "String " + s;
  }

  static String chars(CharSequence cs) {
    return "length " + cs.length();
  }

  static String chars(int n) {
    return "int " + n;
  }

  public static void main(String[] args) {
    int n = 3;
    // an int argument matches an Integer parameter (boxing)
    System.out.println(box(n));
    // "new X().method()" as an argument has the method's type, not X
    System.out.println(chars(new StringBuilder("abc").reverse()));
    System.out.println(chars(new StringBuilder("abcd").length()));
  }
}
