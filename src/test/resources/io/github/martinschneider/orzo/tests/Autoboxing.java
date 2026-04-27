package io.github.martinschneider.orzo.tests;

public class Autoboxing extends AutoboxBase {
  public Autoboxing(boolean b) {
    super(b);
  }

  public static void main(String[] args) {
    Autoboxing a = new Autoboxing(true);
    System.out.println(a.val);
    Autoboxing b = new Autoboxing(false);
    System.out.println(b.val);
  }
}
