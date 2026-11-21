package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;

public class ClassLiteralChains {
  public static void main(String[] args) {
    System.out.println(String.class.getSimpleName());
    System.out.println(ArrayList.class.getName().length());
    Class<?> c = java.util.List.class;
    System.out.println(c.getSimpleName());
    System.out.println(java.util.Map.class.getSimpleName());
    int max = java.lang.Integer.MAX_VALUE;
    System.out.println(max);
    System.out.println(java.io.File.separator.length());
    Class<?> v = void.class;
    System.out.println(v == Void.TYPE);
    System.out.println(int.class.getName());
  }
}
