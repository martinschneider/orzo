package io.github.martinschneider.orzo.tests;

// method references: array constructors, on expressions, with type arguments
import java.util.*;
import java.util.function.*;

public class MethodReferenceForms {
  static class Foo {
    String a = "A";
    String getA() { return a; }
  }
  static class Box<T> {
    T x;
    Box(T x) { this.x = x; }
    T getX() { return x; }
  }
  public static void main(String[] args) {
    IntFunction<int[]> mk = int[]::new;
    System.out.println(mk.apply(3).length);
    Function<Integer, String[]> mk2 = String[]::new;
    System.out.println(mk2.apply(2).length);
    Supplier<String> s = new Foo()::getA;
    System.out.println(s.get());
    Foo f = new Foo();
    Supplier<String> t = (f)::getA;
    f.a = "B";
    System.out.println(t.get());
    Function<Box<String>, String> g = Box<String>::getX;
    System.out.println(g.apply(new Box<>("bx")));
    Supplier<List<Double>> l = LinkedList<Double>::new;
    System.out.println(l.get().size());
    Integer[] arr = List.of(3, 1, 2).stream().sorted().toArray(Integer[]::new);
    System.out.println(arr[0] + " " + arr.length);
  }
}
