package io.github.martinschneider.orzo.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class TryFinally {
  static int counter = 0;

  static int withReturn() {
    try {
      counter++;
      return counter * 10;
    } finally {
      counter += 100;
    }
  }

  static void withException() {
    try {
      System.out.println("before");
      Integer.parseInt("x");
      System.out.println("not reached");
    } catch (NumberFormatException e) {
      System.out.println("caught");
    } finally {
      System.out.println("finally 1");
    }
  }

  static void rethrow() {
    try {
      try {
        Integer.parseInt("y");
      } finally {
        System.out.println("finally 2");
      }
    } catch (NumberFormatException e) {
      System.out.println("outer caught");
    }
  }

  static void rethrowVariable() {
    try {
      Integer.parseInt("z");
    } catch (NumberFormatException e) {
      RuntimeException wrapped = new RuntimeException("wrapped");
      throw wrapped;
    }
  }

  public static void main(String[] args) throws IOException {
    System.out.println(withReturn());
    System.out.println(counter);
    withException();
    rethrow();
    try {
      rethrowVariable();
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (PrintStream ps = new PrintStream(bytes)) {
      ps.print("resource");
    }
    System.out.println(bytes.toString());
  }
}
