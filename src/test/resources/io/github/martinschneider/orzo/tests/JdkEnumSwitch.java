package io.github.martinschneider.orzo.tests;

import java.util.concurrent.TimeUnit;

public class JdkEnumSwitch {
  static String describe(TimeUnit unit) {
    switch (unit) {
      case SECONDS:
        return "s";
      case MINUTES:
      case HOURS:
        return "long";
      default:
        return "other";
    }
  }

  public static void main(String[] args) {
    System.out.println(describe(TimeUnit.SECONDS));
    System.out.println(describe(TimeUnit.HOURS));
    System.out.println(describe(TimeUnit.DAYS));
  }
}
