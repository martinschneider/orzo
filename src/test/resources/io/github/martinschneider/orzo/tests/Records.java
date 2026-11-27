package io.github.martinschneider.orzo.tests;

// records: canonical and compact constructors, accessors, toString(), equals() and hashCode()
import java.util.*;

public class Records {
  record Point(int x, int y) {
    Point {
      if (x < 0) throw new IllegalArgumentException("x");
    }
    int sum() { return x + y; }
    static Point origin() { return new Point(0, 0); }
  }

  record Named(String name, double weight, List<String> tags) implements Comparable<Named> {
    public int compareTo(Named o) { return name.compareTo(o.name); }
  }

  record Empty() {}

  record Pair<A, B>(A first, B second) {}

  record Doubled(int a, int b) {
    Doubled(int a, int b) {
      this.a = a;
      this.b = b * 2;
    }

    int plus(int x, int y) {
      return a + b + x + y;
    }
  }

  record Checked(String s) {
    public Checked {
      s = s.trim();
    }
  }

  record Arrays(int[] a, String... b) {}

  record Custom(int v) {
    public String toString() { return "C" + v; }
    public int v() { return v * 10; }
  }

  interface Shape { double area(); }
  record Square(double side) implements Shape {
    public double area() { return side * side; }
  }

  public static void main(String[] args) {
    Point p = new Point(1, 2);
    System.out.println(p + " " + p.x() + " " + p.y() + " " + p.sum() + " " + Point.origin());
    System.out.println(p.equals(new Point(1, 2)) + " " + p.equals(new Point(2, 1)) + " " + (p.hashCode() == new Point(1, 2).hashCode()));
    Named n = new Named("a", 1.5, List.of("t"));
    System.out.println(n + " " + n.equals(new Named("a", 1.5, List.of("t"))) + " " + n.compareTo(new Named("b", 0, List.of())));
    System.out.println(new Empty() + " " + new Empty().equals(new Empty()) + " " + new Empty().hashCode());
    System.out.println(new Custom(4) + " " + new Custom(4).v());
    Shape s = new Square(3);
    System.out.println(s.area() + " " + (s instanceof Record) + " " + p.hashCode());
    try {
      new Point(-1, 0);
    } catch (IllegalArgumentException e) {
      System.out.println("bad " + e.getMessage());
    }
    Pair<String, Integer> pair = new Pair<>("a", 1);
    System.out.println(pair + " " + pair.first() + " " + new Arrays(new int[] {1}, "x", "y").b().length);
    System.out.println(new Doubled(1, 2) + " " + new Doubled(1, 2).plus(3, 4) + " " + new Checked(" c "));
    record Local(String s) {}
    System.out.println(new Local("l"));
  }
}
