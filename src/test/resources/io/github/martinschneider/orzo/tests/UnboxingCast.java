package io.github.martinschneider.orzo.tests;

public class UnboxingCast {
  Object val = 'x';
  public static void main(String[] args) {
    Object o = 'y';
    char a = (char) o;
    Object n = 42;
    int i = (int) n;
    UnboxingCast r = new UnboxingCast();
    char b = (char) r.val;
    Object self = r;
    char c = (char) ((UnboxingCast) self).val;
    System.out.println(a + " " + i + " " + b + " " + c);
  }
}
