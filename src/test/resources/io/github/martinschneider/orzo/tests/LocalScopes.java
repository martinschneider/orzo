package io.github.martinschneider.orzo.tests;

public class LocalScopes {
  public static void main(String[] args) {
    int sum = 0;
    // sibling blocks may declare variables of the same name
    for (int i = 0; i < 3; i++) {
      sum += i;
    }
    for (int i = 10; i < 12; i++) {
      sum += i;
    }
    {
      int y = 100;
      sum += y;
    }
    {
      int y = 1000;
      sum += y;
    }
    // the counter of a repeat loop doesn't clash with (or overwrite) a variable "i"
    int i = 5;
    repeat 3 {
      sum++;
    }
    System.out.println(sum + " " + i);
  }
}
