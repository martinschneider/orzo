package io.github.martinschneider.orzo.tests;

public class SwitchExpressionArguments {
  // switch expressions with block and throw cases as arguments (operands on the stack), switches
  // on boxed values, constant final instance fields, for-each loops over array creations
  private final int value = 2;
  private final int field =
      id(
          switch (value) {
            case 0 -> -1;
            case 2 -> {
              int temp = 0;
              temp += 3;
              yield temp;
            }
            default -> throw new IllegalStateException();
          });

  int id(int i) {
    return i;
  }

  static String fallThrough(Integer p) {
    String result = "";
    return switch (p) {
      case 0:
        result += "0";
      case 1:
        result += "1";
        yield result;
      default:
        yield "other";
    };
  }

  public static void main(String[] args) {
    SwitchExpressionArguments s = new SwitchExpressionArguments();
    System.out.println(s.field + " " + s.value);
    System.out.println(fallThrough(0) + " " + fallThrough(1) + " " + fallThrough(3));
    Character c = 'b';
    switch (c) {
      case 'a' -> System.out.println("a");
      case 'b' -> System.out.println("b");
      default -> System.out.println("?");
    }
    for (int i : new int[] {1, 2}) {
      System.out.println(s.id(switch (i) { case 1 -> 10; default -> { yield 20 + i; } }));
    }
  }
}
