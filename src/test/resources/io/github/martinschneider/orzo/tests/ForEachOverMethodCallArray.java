package io.github.martinschneider.orzo.tests;

import java.lang.reflect.Field;

public class ForEachOverMethodCallArray {
  int x;
  int y;

  public static void main(String[] args) {
    StringBuilder sb = new StringBuilder();
    Class<?> clazz = ForEachOverMethodCallArray.class;
    for (Field field : clazz.getDeclaredFields()) {
      try {
        sb.append(field.getName()).append(",");
      } catch (Exception e) {
      }
    }
    System.out.println(sb.toString());
  }
}
