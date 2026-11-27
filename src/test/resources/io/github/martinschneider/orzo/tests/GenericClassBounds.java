package io.github.martinschneider.orzo.tests;

import java.io.Serializable;
import java.util.*;

public class GenericClassBounds {
  // type parameters with bounds with type arguments, several bounds
  static class Pair<A, B> { A a; B b; Pair(A a, B b) { this.a = a; this.b = b; } }
  static abstract class Base<K, V, I extends Pair<K, V>, I2 extends Pair<V, K>> {
    abstract I make(K k, V v);
  }
  static class Box<X extends Number & Comparable<X>, Y extends List<Map<String, X>>> {
    X max(X a, X b) { return a.doubleValue() > b.doubleValue() ? a : b; }
  }
  static class Node<N extends Node<N>> implements Serializable {}
  public static void main(String[] args) {
    System.out.println(new Box<Integer, List<Map<String, Integer>>>().max(3, 7));
  }
}
