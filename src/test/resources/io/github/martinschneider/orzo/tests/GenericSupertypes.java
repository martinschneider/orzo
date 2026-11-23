package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.function.Supplier;

public class GenericSupertypes {
  // supertypes with type arguments and qualified names, several superinterfaces
  interface Named {
    String name();
  }

  interface Described extends Named, Comparable<Described> {
    String description();
  }

  static class Names extends ArrayList<String> implements java.io.Serializable {}

  static class Item implements Described, Supplier<String> {
    public String name() {
      return "item";
    }

    public String description() {
      return "an " + name();
    }

    public int compareTo(Described other) {
      return name().compareTo(other.name());
    }

    public String get() {
      return description();
    }
  }

  public static void main(String[] args) {
    Names names = new Names();
    names.add("a");
    names.add("b");
    System.out.println(names.size() + " " + names.get(1));
    Item item = new Item();
    System.out.println(item.get() + " " + item.compareTo(new Item()));
  }
}
