package io.github.martinschneider.orzo.tests;

public class TernaryArrayStore {
  public static void main(String[] args) {
    // a conditional value stored into an array element (the array and the index are on the stack
    // where the branches join)
    int[] values = new int[3];
    boolean flag = values.length == 3;
    values[0] = flag ? 1 : 2;
    values[values.length - 1] = !flag ? 3 : 4;
    String[] names = new String[2];
    names[1] = flag ? "yes" : "no";
    System.out.println(values[0] + " " + values[1] + " " + values[2]);
    System.out.println(names[1]);
  }
}
