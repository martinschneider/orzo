package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class LocalClasses {
  String name = "outer";

  interface Shape {
    double area();
  }

  // local classes, using captured local variables and the members of their enclosing instance
  List<String> run(int scale) {
    List<String> log = new ArrayList<>();
    String prefix = "> ";
    class Entry {
      String text;
      int size = scale;

      Entry(String text) {
        this.text = text;
      }

      Entry() {
        this("empty");
      }

      void add() {
        log.add(prefix + text + " " + size + " " + name);
      }
    }
    new Entry("first").add();
    Entry second = new Entry();
    second.add();
    return log;
  }

  static double total(double[] sides) {
    class Square implements Shape {
      double side;

      Square(double side) {
        this.side = side;
      }

      public double area() {
        return side * side;
      }
    }
    double sum = 0;
    for (double side : sides) {
      sum += new Square(side).area();
    }
    return sum;
  }

  // (captured variables used in all kinds of statements)
  static String statements(int a, int[] values, String text) {
    int b = 2;
    String sep = ",";
    class Walker {
      String walk() {
        String result = "";
        int i = 0;
        while (i < a) {
          i++;
        }
        do {
          i += b;
        } while (i < a * b);
        for (int j = 0; j < values.length; j++) {
          result += values[j] + sep;
        }
        for (int value : values) {
          if (value > b) {
            result += "big" + sep;
          } else {
            result += "small" + sep;
          }
        }
        switch (a) {
          case 1:
            result += "one";
            break;
          default:
            result += text;
        }
        try {
          if (a > 100) {
            throw new IllegalStateException(text);
          }
          result += sep + i;
        } catch (IllegalStateException e) {
          result += e.getMessage() + sep;
        } finally {
          result += sep + b;
        }
        return result;
      }
    }
    return new Walker().walk();
  }

  // (another local class named Square: "LocalClasses$2Square")
  static String name() {
    class Square {
      String name() {
        return "square";
      }
    }
    return new Square().name();
  }

  public static void main(String[] args) {
    System.out.println(new LocalClasses().run(3));
    System.out.println(statements(3, new int[] {1, 5}, "x"));
    System.out.println(total(new double[] {1, 2, 3}) + " " + name());
  }
}
