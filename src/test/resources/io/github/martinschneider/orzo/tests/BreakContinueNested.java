package io.github.martinschneider.orzo.tests;

public class BreakContinueNested {
  public static void main(String[] args) {
    // break nested inside an if, inside a for loop
    for (int i = 0; i < 10; i++) {
      if (i == 3) {
        break;
      }
      System.out.println(i);
    }

    // continue nested inside an if, inside a for loop
    for (int i = 0; i < 6; i++) {
      if (i == 2 || i == 4) {
        continue;
      }
      System.out.println(i);
    }

    // break inside a switch inside a for loop should only exit the switch, not the loop
    for (int i = 0; i < 4; i++) {
      switch (i) {
        case 1:
          System.out.println(100);
          break;
        default:
          System.out.println(i);
      }
    }

    // continue inside a switch inside a for loop should continue the outer loop
    for (int i = 0; i < 5; i++) {
      switch (i) {
        case 2:
          continue;
      }
      System.out.println(i);
    }
  }
}
