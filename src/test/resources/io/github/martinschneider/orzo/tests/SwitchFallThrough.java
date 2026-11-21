package io.github.martinschneider.orzo.tests;

public class SwitchFallThrough {
  static String describe(int x) {
    String s = "";
    switch (x) {
      case 1:
        s += "one ";
      case 2:
        s += "two ";
        break;
      case 3:
      case 4:
        s += "three-or-four ";
      default:
        s += "default";
    }
    return s;
  }

  public static void main(String[] args) {
    System.out.println(describe(1));
    System.out.println(describe(2));
    System.out.println(describe(4));
    // println() without arguments prints a line break
    System.out.println();
    System.out.println(describe(9));
  }
}
