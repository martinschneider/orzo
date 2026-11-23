package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InterfaceMethods {
  // default, static and private interface methods
  interface Named {
    String name();

    default String greet() {
      return "hi " + name() + suffix();
    }

    private String suffix() {
      return "!";
    }

    static Named of(String name) {
      return () -> name;
    }
  }

  abstract static class Base {
    abstract String kind();

    String describe() {
      return "a " + kind();
    }
  }

  static class Cat extends Base implements Named {
    public String name() {
      return "cat";
    }

    String kind() {
      return "pet";
    }

    // (inherited methods, unqualified and with "this.")
    String all() {
      return greet() + " " + describe() + " " + this.greet() + " " + this.kind();
    }
  }

  public static void main(String[] args) {
    System.out.println(Named.of("x").greet());
    System.out.println(new Cat().all());
    List<String> words = new ArrayList<>(List.of("ccc", "a", "bb"));
    Comparator<String> byLength = (a, b) -> a.length() - b.length();
    words.sort(byLength);
    words.add(0, "dddd");
    words.sort((a, b) -> b.compareTo(a));
    System.out.println(words);
  }
}
