package io.github.martinschneider.orzo.tests;

public class Asserts {
  static int checks;

  static boolean count() {
    checks++;
    return true;
  }

  static class Inner {
    void check(int x) {
      assert x > 0 : "x=" + x;
    }
  }

  interface Shape {
    default int sides(int n) {
      assert n >= 3;
      return n;
    }
  }

  enum Color {
    RED;

    int id(int i) {
      assert i == 0 : i;
      return i;
    }
  }

  public static void main(String[] args) {
    assert count();
    System.out.println("enabled " + (checks == 1));
    try {
      new Inner().check(-1);
    } catch (AssertionError e) {
      System.out.println("inner " + e.getMessage());
    }
    try {
      new Shape() {}.sides(2);
    } catch (AssertionError e) {
      System.out.println("interface " + e.getMessage());
    }
    try {
      Color.RED.id(7);
    } catch (AssertionError e) {
      System.out.println("enum " + e.getMessage());
    }
    String[] none = {};
    Runnable r = () -> {
      assert none.length > 5 : none.length > 3 ? "many" : "few";
    };
    try {
      r.run();
    } catch (AssertionError e) {
      System.out.println("lambda " + e.getMessage());
    }
    if (checks > 5) assert false; else System.out.println("else ok");
    assert(checks == 1);
    System.out.println("done");
  }
}
