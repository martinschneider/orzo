package io.github.martinschneider.orzo.tests;

public class DefinitelyAssigned {
  static boolean c(int i) {
    return i % 2 == 0;
  }

  // variables declared without a value, definitely assigned before they are used
  public static void main(String[] args) {
    int a;
    if (c(1)) {
      a = 1;
    } else {
      a = 2;
    }
    int b;
    while (true) {
      b = 3;
      break;
    }
    int d;
    boolean e;
    if (c(2) && (e = true)) {
      d = e ? 4 : 5;
    } else {
      d = 6;
    }
    int f;
    try {
      f = Integer.parseInt("7");
    } catch (NumberFormatException ex) {
      f = 0;
    }
    int g;
    switch (a) {
      case 1:
        g = 8;
        break;
      default:
        g = 9;
    }
    int h;
    for (; ; ) {
      if (c(4)) {
        h = 10;
        break;
      }
    }
    int x, y = 1, 2;
    x, y = y, x;
    System.out.println(a + " " + b + " " + d + " " + f + " " + g + " " + h + " " + x + " " + y);
    // (parallel: the old values, also in a call's argument)
    double p, q = 4, 9;
    p, q = √q, √(p + Math.abs(-p));
    System.out.println(p + " " + q);
  }
}
