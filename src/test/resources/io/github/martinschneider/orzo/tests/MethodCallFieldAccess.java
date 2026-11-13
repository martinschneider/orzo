package io.github.martinschneider.orzo.tests;

public class MethodCallFieldAccess {
  static Holder make(String s) {
    Holder h = new Holder();
    h.val = s;
    return h;
  }

  public static void main(String[] args) {
    System.out.println(make("hello").val);
  }
}
