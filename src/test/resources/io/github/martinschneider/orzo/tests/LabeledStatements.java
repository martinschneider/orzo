package io.github.martinschneider.orzo.tests;

public class LabeledStatements {
  // labels on any statement, and finally blocks run by break and continue
  public static void main(String[] args) {
    L8:
    break L8;
    int j = 2;
    OUTER:
    switch (j) {
      case 2:
        for (int i = 0; i < 3; i++) {
          if (i == 1) break OUTER;
          System.out.println(i);
        }
        System.out.println("no");
      default:
        System.out.println("no2");
    }
    b:
    try {
      if (j > 1) break b;
      System.out.println("no3");
    } finally {
      System.out.println("fin");
    }
    x:
    if (j > 0) {
      System.out.println("in");
      break x;
    }
    for (int i = 0; i < 3; i++) {
      try {
        try {
          if (i == 0) continue;
          if (i == 2) break;
          System.out.println("body " + i);
        } finally {
          System.out.println("inner " + i);
        }
      } finally {
        System.out.println("outer " + i);
      }
    }
    System.out.println("end");
  }
}
