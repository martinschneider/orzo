package io.github.martinschneider.orzo.tests;

public class OtherFieldAccess {
  int value;
  boolean flag;

  OtherFieldAccess(int value, boolean flag) {
    this.value = value;
    this.flag = flag;
  }

  public static void main(String[] args) {
    OtherFieldAccess a = new OtherFieldAccess(10, true);
    OtherFieldAccess b = new OtherFieldAccess(20, false);
    System.out.println(a.value);
    System.out.println(b.flag);
    int sum = a.value + b.value;
    System.out.println(sum);
  }
}
