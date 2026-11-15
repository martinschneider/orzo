package io.github.martinschneider.orzo.tests;

public class EnumSwitch {
  public static void main(String[] args) {
    Color c1 = Color.GREEN;
    switch (c1) {
      case RED:
        System.out.println("red");
        break;
      case GREEN:
        System.out.println("green");
        break;
      case BLUE:
        System.out.println("blue");
        break;
    }
    Color c2 = Color.RED;
    switch (c2) {
      case RED:
      case BLUE:
        System.out.println("warm-or-cool");
        break;
      default:
        System.out.println("other");
    }
    Color c3 = Color.GREEN;
    switch (c3) {
      case RED:
      case BLUE:
        System.out.println("warm-or-cool");
        break;
      default:
        System.out.println("other");
    }
  }
}
