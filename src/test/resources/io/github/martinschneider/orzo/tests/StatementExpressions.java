package io.github.martinschneider.orzo.tests;

public class StatementExpressions {
  // expression statements: object creations (also of inner classes of an object), assignments to
  // fields of new objects and calls, parenthesized variables
  static int made;

  class Inner {
    Inner() {
      made++;
    }
  }

  static class Box {
    int i;
  }

  static Box get(Box box) {
    return box;
  }

  public static void main(String[] args) {
    StatementExpressions outer = new StatementExpressions();
    outer.new Inner();
    new Box().i = 5;
    Box box = new Box();
    get(box).i = 3;
    int i = 1;
    (i)++;
    (i)--;
    int j = (i)++ + 1;
    int a, b;
    (a) = (b) = 4;
    System.out.println(made + " " + box.i + " " + i + " " + j + " " + (a + b));
  }
}
