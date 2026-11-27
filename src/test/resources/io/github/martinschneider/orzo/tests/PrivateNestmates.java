package io.github.martinschneider.orzo.tests;

import java.util.function.Supplier;

public class PrivateNestmates {
  private int secret = 42;

  private static String hidden() {
    return "h";
  }

  static class Nested {
    private int own = 7;

    String peek(PrivateNestmates outer) {
      return hidden() + outer.secret;
    }
  }

  class Inner {
    private String name() {
      return "inner " + secret;
    }
  }

  // private members accessed by nested, inner, local and anonymous classes and lambdas (and the
  // other way round)
  void run() {
    Nested nested = new Nested();
    System.out.println(nested.peek(this) + " " + nested.own + " " + new Inner().name());
    class Local {
      private int twice() {
        return 2 * secret;
      }
    }
    Runnable anonymous =
        new Runnable() {
          public void run() {
            System.out.println(new Local().twice() + " " + hidden() + secret);
          }
        };
    anonymous.run();
    Supplier<String> lambda = () -> hidden() + "!";
    System.out.println(lambda.get());
  }

  public static void main(String[] args) {
    new PrivateNestmates().run();
  }
}
