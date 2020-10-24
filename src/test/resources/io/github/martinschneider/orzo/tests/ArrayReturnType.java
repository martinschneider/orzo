package io.github.martinschneider.orzo.tests;

public class ArrayReturnType {
  public static void main(String[] args) {
    System.out.println(getArray()[0]);
  }
  
  public static int[] getArray()
  {
  	return new int[]{42};
  }
}