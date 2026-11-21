package io.github.martinschneider.orzo.tests;

public class FieldArrayAssignment {
  int[] small = new int[2];
  FieldArrayAssignment self;
  public static void main(String[] args) {
    FieldArrayAssignment r = new FieldArrayAssignment();
    r.self = r;
    r.small[1] = 5;
    r.self.small[0] = 7;
    r.small[1]++;
    System.out.println(r.small[0] + " " + r.small[1]);
  }
}
