package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;

public class QualifiedCast {
  public static void main(String[] args) {
    Object o = new ArrayList<String>();
    System.out.println(((java.util.List) o).size());
    Object s = "";
    boolean b = true;
    if (b && ((java.lang.String) s).isEmpty()) {
      System.out.println("empty");
    }
    java.lang.reflect.Type t = String.class;
    if (t instanceof Class && ((java.lang.Class) t).getSimpleName().length() == 6) {
      System.out.println("class");
    }
  }
}
