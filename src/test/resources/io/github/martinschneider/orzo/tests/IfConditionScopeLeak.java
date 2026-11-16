package io.github.martinschneider.orzo.tests;

public class IfConditionScopeLeak {
  private static char character = 'X';

  public static void run() {
    if (character == 'X') {
      char character;
      character = 'Y';
      System.out.println(character);
    }
  }

  public static void main(String[] args) {
    run();
  }
}
