package io.github.martinschneider.orzo.tests;

public class AbstractClasses {
  // abstract classes and methods
  abstract static class Shape {
    abstract double area();

    abstract String name();

    String describe() {
      return name() + " " + area();
    }
  }

  static class Square extends Shape {
    double side;

    Square(double side) {
      this.side = side;
    }

    double area() {
      return side * side;
    }

    String name() {
      return "square";
    }
  }

  public abstract static class Counter {
    public abstract int next();
  }

  static class Ones extends Counter {
    public int next() {
      return 1;
    }
  }

  public static void main(String[] args) {
    Shape shape = new Square(3);
    System.out.println(shape.describe());
    Counter counter = new Ones();
    System.out.println(counter.next() + counter.next());
  }
}
