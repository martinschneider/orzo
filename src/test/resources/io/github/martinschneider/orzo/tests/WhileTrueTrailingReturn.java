package io.github.martinschneider.orzo.tests;

public class WhileTrueTrailingReturn {
  private static int countTo(int target) {
    int i = 0;
    while (true) {
      if (i >= target) {
        return i;
      }
      i = i + 1;
    }
  }

  public static void main(String[] args) {
    System.out.println(countTo(5));
  }
}
