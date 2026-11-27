package io.github.martinschneider.orzo.tests;

public class OrzoKeywordsAsNames {
  // Orzo's keywords "repeat" and "unless" are Java names unless they start an Orzo statement
  static int calls;

  static int repeat(int x) {
    calls++;
    return x * 2;
  }

  static boolean unless(boolean b) {
    calls++;
    return !b;
  }

  public static void main(String[] args) {
    int repeat = 2;
    repeat++;
    repeat += repeat(3);
    boolean unless = repeat < 10;
    unless(unless);
    if (unless) System.out.println(repeat + 1);
    System.out.println(unless(false) + " " + calls + " " + "ab".repeat(repeat - 7));
    repeat 2 {
      calls++;
    }
    unless (calls > 5) {
      System.out.println("unless " + calls);
    }
  }
}
