package io.github.martinschneider.orzo.tests;

import java.lang.annotation.*;

// annotation types with default values, and selectors after calls on class literals
public class AnnotationTypes {
  enum Color { RED, GREEN }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Info {
    int value() default 2;
    String name() default "x";
    Color color() default Color.RED;
    Class<?> type() default int.class;
    String[] tags() default {"a", "b"};
    Deprecated[] none() default {};
  }

  @interface Marker {}

  @Info(value = 3, tags = {})
  @Marker
  static void m() {}

  public static void main(String[] args) {
    m();
    System.out.println(AnnotationTypes.class.getInterfaces().length);
    System.out.println(String.class.getInterfaces()[0].getSimpleName().length());
    System.out.println(Info.class.isAnnotation() + " " + Info.class.isInterface() + " " + Marker.class.getInterfaces()[0].getName());
  }
}
