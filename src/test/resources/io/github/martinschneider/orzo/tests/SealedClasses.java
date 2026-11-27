package io.github.martinschneider.orzo.tests;

// sealed classes and interfaces ("sealed", "non-sealed" and "permits" are ignored)
public class SealedClasses {
  sealed interface Shape permits Circle, Square, Other {}
  record Circle(double r) implements Shape {}
  static final class Square implements Shape {
    public String toString() { return "Square"; }
  }
  static non-sealed class Other implements Shape {
    public String toString() { return "Other"; }
  }
  static sealed abstract class Base permits Sub {}
  static final class Sub extends Base {}
  public static void main(String[] args) {
    int sealed = 1, permits = 2;
    Shape[] shapes = {new Circle(1), new Square(), new Other()};
    for (Shape s : shapes) {
      System.out.println(s instanceof Circle c ? "circle " + c.r() : s.toString());
    }
    System.out.println(sealed + permits + " " + (new Sub() instanceof Base));
  }
}
