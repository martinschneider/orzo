package io.github.martinschneider.orzo.tests;

public class StringConcatWithCall {
  public static void main(String[] args) {
    String s = "x12";
    int idx = 1;
    System.out.println("ok " + s.substring(idx));
    System.out.println("n=" + Integer.parseInt(s.substring(idx)));
    String r = "len " + "abc".length();
    System.out.println(r);
    System.out.println("abc".toUpperCase() + s.length());
  }
}
