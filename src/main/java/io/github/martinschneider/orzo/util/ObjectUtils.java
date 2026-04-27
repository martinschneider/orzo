package io.github.martinschneider.orzo.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class ObjectUtils {

  private ObjectUtils() {}

  public static int hashCode(Object obj) {
    if (obj == null) return 0;
    int prime = 31;
    int result = 1;
    Class<?> clazz = obj.getClass();
    while (clazz != null && clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;
        try {
          result = prime * result + Objects.hashCode(field.get(obj));
        } catch (IllegalAccessException e) {
          // skip inaccessible fields
        }
      }
      clazz = clazz.getSuperclass();
    }
    return result;
  }

  public static boolean equals(Object a, Object b) {
    if (a == b) return true;
    if (a == null || b == null) return false;
    if (a.getClass() != b.getClass()) return false;
    Class<?> clazz = a.getClass();
    while (clazz != null && clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;
        try {
          if (!Objects.equals(field.get(a), field.get(b))) return false;
        } catch (IllegalAccessException e) {
          return false;
        }
      }
      clazz = clazz.getSuperclass();
    }
    return true;
  }

  public static String toString(Object obj) {
    if (obj == null) return "null";
    StringBuilder sb = new StringBuilder(obj.getClass().getSimpleName());
    sb.append('[');
    boolean first = true;
    Class<?> clazz = obj.getClass();
    while (clazz != null && clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;
        try {
          if (!first) sb.append(", ");
          sb.append(field.getName()).append('=').append(field.get(obj));
          first = false;
        } catch (IllegalAccessException e) {
          // skip inaccessible fields
        }
      }
      clazz = clazz.getSuperclass();
    }
    sb.append(']');
    return sb.toString();
  }
}
