package io.github.martinschneider.orzo.tests;

public class SamePackageCast {
  static int value(SamePackageCastBox box) {
    return box.v;
  }

  public static void main(String[] args) {
    Object o = new SamePackageCastBox();
    // a cast to a class of the same package (without an import) as an argument
    System.out.println(value((SamePackageCastBox) o));
    SamePackageCastBox box = (SamePackageCastBox) o;
    System.out.println(box.v + 1);
  }
}
