package io.github.martinschneider.orzo.tests;

public class CharNotEqualAssignment {
  private static int count = 0;

  private static char next() {
    count = count + 1;
    if (count == 1) {
      return 'a';
    }
    if (count == 2) {
      return 'b';
    }
    if (count == 3) {
      return '\'';
    }
    return 'c';
  }

  public static void main(String[] args) {
    char c;
    while ((c = next()) != '\'') {
      System.out.println(c);
    }
    System.out.println("done");
  }
}
