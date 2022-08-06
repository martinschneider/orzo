package io.github.martinschneider.orzo.util;

// don't use generics to allow compilation with Orzo
public class Pair {
  public Object _left;
  public Object _right;

  public Pair(Object left, Object right) {
    this._left = left;
    this._right = right;
  }
}
