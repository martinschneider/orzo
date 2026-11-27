package io.github.martinschneider.orzo.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CheckedExceptions {
  static class AppException extends Exception {
    AppException(String message) {
      super(message);
    }
  }

  static class Values {
    private final int a, b, c;

    Values() {
      a = b = c = 1; // (definitely assigned)
    }

    Values(String unused) {
      throw new IllegalStateException("never"); // (assigns nothing, completes abruptly)
    }

    int sum() {
      return a + b + c;
    }
  }

  static void open(String name) throws IOException {
    if (name.isEmpty()) {
      throw new FileNotFoundException("empty");
    }
  }

  static int check(int n) throws AppException {
    if (n < 0) {
      throw new AppException("negative");
    }
    return n;
  }

  static String rethrow(String name) throws IOException {
    try {
      open(name);
      return "opened " + name;
    } catch (IOException e) {
      throw e; // (rethrows an IOException only)
    }
  }

  static int forever() {
    int i = 0;
    do {
      if (++i > 3) {
        return i;
      }
    } while (true);
  }

  static int forever2() {
    for (int i = 0; ; i++) {
      if (i > 4) {
        return i;
      }
    }
  }

  interface Task {
    void run();
  }

  // (Object.wait() on an interface throws InterruptedException)
  static String waitOn(Task task) {
    try {
      task.wait(1);
      return "waited";
    } catch (InterruptedException e) {
      return "interrupted";
    } catch (IllegalMonitorStateException e) {
      return "not the owner";
    }
  }

  // (a finally block that returns discards the exception)
  static String discarded() {
    try {
      check(-1);
    } finally {
      return "discarded";
    }
  }

  // checked exceptions caught or declared
  public static void main(String[] args) throws Exception {
    try {
      open("");
    } catch (FileNotFoundException e) {
      System.out.println("caught " + e.getMessage());
    } finally {
      System.out.println("finally");
    }
    try {
      check(-1);
    } catch (AppException e) {
      System.out.println("caught " + e.getMessage());
    }
    System.out.println(rethrow("a") + " " + check(2) + " " + forever() + " " + forever2());
    Callable<Integer> callable = () -> check(5);
    System.out.println(callable.call() + " " + new Values().sum());
    System.out.println(waitOn(() -> {}) + " " + discarded());
  }
}
