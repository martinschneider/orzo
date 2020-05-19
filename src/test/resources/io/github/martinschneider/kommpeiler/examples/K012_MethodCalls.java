package io.github.martinschneider.kommpeiler.examples;

public class K012_MethodCalls {
  public static void main(String[] args) {
    int j = 100;
    System.out.println(getResult(100));
    System.out.println(getResult(getResult(j)));
    System.out.println(getResult(getResult(getResult(j))));
  }

  public static int getResult(int i) {
    return i + 1;
  }
}
