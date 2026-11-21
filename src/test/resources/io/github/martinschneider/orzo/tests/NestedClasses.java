package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class NestedClasses {
  public static class Point {
    public final int x;
    public int y;

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int sum() {
      return x + y;
    }
  }

  private static final class Frame {
    final List<String> locals;

    Frame(List<String> locals) {
      this.locals = locals;
    }
  }

  private final TreeMap<Integer, Frame> frames = new TreeMap<>();

  static Point twice(Point p) {
    return new Point(p.x * 2, p.y * 2);
  }

  public static void main(String[] args) {
    Point p = new Point(1, 2);
    p.y = 5;
    Point q = twice(p);
    System.out.println(p.sum() + " " + q.x + " " + q.sum());
    NestedClasses n = new NestedClasses();
    List<String> l = new ArrayList<>();
    l.add("a");
    n.frames.put(3, new Frame(l));
    Frame f = n.frames.get(3);
    System.out.println(f.locals.get(0) + " " + n.frames.get(3).locals.size());
  }
}
