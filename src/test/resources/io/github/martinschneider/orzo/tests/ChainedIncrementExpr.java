package io.github.martinschneider.orzo.tests;

public class ChainedIncrementExpr {
  int counter;
  ChainedIncrementExpr self;
  static int[] arr = new int[2];
  public static void main(String[] args) {
    ChainedIncrementExpr r = new ChainedIncrementExpr();
    r.self = r;
    int a = ++r.counter;
    int b = r.self.counter++;
    int c = arr[1]++;
    int d = ++arr[1];
    System.out.println(a + " " + b + " " + r.counter + " " + c + " " + d + " " + arr[1]);
  }
}
