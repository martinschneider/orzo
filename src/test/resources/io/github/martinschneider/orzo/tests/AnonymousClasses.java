package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnonymousClasses {
  static int created = 0;
  String name = "outer";
  int count = 0;

  abstract static class Shape {
    String label;

    Shape(String label) {
      this.label = label;
    }

    abstract double area();

    String describe() {
      return label + " " + area();
    }
  }

  interface Greeter {
    String greet(String who);
  }

  String hello() {
    return "hello from " + name;
  }

  // anonymous classes implementing an interface or extending a class, using captured local
  // variables and the members of their enclosing instance
  void run() {
    String suffix = "!";
    int[] calls = {0};
    Greeter greeter =
        new Greeter() {
          int greeted = 0;

          public String greet(String who) {
            greeted++;
            calls[0]++;
            count++;
            return "hi " + who + suffix + " " + greeted + " " + hello();
          }
        };
    System.out.println(greeter.greet("a"));
    System.out.println(greeter.greet("b"));
    System.out.println(calls[0] + " " + count);
    double side = 3;
    Shape square =
        new Shape("square") {
          double area() {
            return side * side;
          }
        };
    System.out.println(square.describe());
  }

  static Runnable counter(List<String> log) {
    return new Runnable() {
      {
        created++;
      }

      public void run() {
        log.add("run " + created);
      }
    };
  }

  public static void main(String[] args) {
    new AnonymousClasses().run();
    List<String> log = new ArrayList<>();
    counter(log).run();
    counter(log).run();
    System.out.println(log);
    List<String> words = new ArrayList<>(List.of("ccc", "a", "bb"));
    words.sort(
        new Comparator<String>() {
          public int compare(String x, String y) {
            return x.length() - y.length();
          }
        });
    System.out.println(words);
  }
}
