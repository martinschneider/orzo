package io.github.martinschneider.orzo.tests;

public class ChainedFieldIncrement {
  public int count;
  public ChainedFieldIncrement inner;

  public static void main(String[] args) {
    ChainedFieldIncrement outer = new ChainedFieldIncrement();
    outer.inner = new ChainedFieldIncrement();
    outer.inner.count++;
    outer.inner.count++;
    outer.count--;
    ++outer.inner.count;
    System.out.println(outer.inner.count);
    System.out.println(outer.count);
    int x = 1;
    ++x;
    --x;
    ++x;
    System.out.println(x);
  }
}
