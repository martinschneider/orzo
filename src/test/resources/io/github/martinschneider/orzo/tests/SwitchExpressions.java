package io.github.martinschneider.orzo.tests;

// switch with "->" cases and switch expressions (with yield)
import java.util.*;

public class SwitchExpressions {
  enum Color { RED, GREEN, BLUE }

  static String name(int n) {
    return switch (n) {
      case 1 -> "one";
      case 2, 3 -> "few";
      default -> {
        String s = "many";
        yield s + n;
      }
    };
  }

  static int score(Color c) {
    int base = switch (c) {
      case RED -> 1;
      case GREEN -> 2;
      case BLUE -> 3;
    };
    return base * 10;
  }

  static int old(String s) {
    return switch (s) {
      case "a":
        yield 1;
      case "b":
      case "c":
        yield 2;
      default:
        for (int i = 0; i < 10; i++) {
          if (i == 4) yield i;
        }
        yield -1;
    };
  }

  public static void main(String[] args) {
    String[] none = {};
    for (int i = 0; i < 5; i++) {
      switch (i) {
        case 0 -> System.out.println("zero");
        case 1, 2 -> {
          System.out.println("small " + i);
        }
        default -> System.out.println("big " + name(i));
      }
    }
    System.out.println(name(1) + name(3) + name(9));
    System.out.println(score(Color.GREEN) + score(Color.BLUE));
    System.out.println(old("a") + old("c") + old("z"));
    long l = 1 + switch (none.length) { case 0 -> 2; default -> 3L; };
    System.out.println(l);
    System.out.println("x" + switch (name(2)) { case "few" -> 'f'; default -> 'o'; });
    try {
      int t = switch (none.length) { case 0 -> throw new IllegalStateException("t"); default -> 1; };
    } catch (IllegalStateException e) {
      System.out.println(e.getMessage());
    }
  }
}
