package io.github.martinschneider.orzo.tests;

public class NestedNames {
  static class Box<T> {
    T value;

    class Getter {
      T get() {
        return value;
      }
    }
  }

  static class StringBox extends Box<String> {
    String read() {
      value = "boxed";
      Getter getter = new Getter();
      String s = (String) getter.get(); // (T of the enclosing class, erased)
      return s;
    }
  }

  // two nested classes of the same name: the one of the enclosing class
  static class First {
    static class Item {
      String name() {
        return "first";
      }
    }

    String item() {
      return new Item().name();
    }
  }

  static class Second {
    static class Item {
      String name() {
        return "second";
      }
    }

    String item() {
      return new Item().name();
    }
  }

  NestedNames() {
    // (the throws clause of a method of an anonymous class isn't the constructor's)
    Object o =
        new Object() {
          void fail() throws Exception {
            throw new Exception();
          }
        };
  }

  public static void main(String[] args) {
    new NestedNames();
    System.out.println(new StringBox().read() + " " + new First().item() + " " + new Second().item());
  }
}
