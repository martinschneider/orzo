package io.github.martinschneider.orzo.tests;

public class SameNamedFieldChain {
  private String count = "own";
  private ChainedFieldIncrement other = new ChainedFieldIncrement();

  int otherCount() {
    // "count" here is the other object's field, not this class's (String) field of the same name
    return other.count;
  }

  public static void main(String[] args) {
    SameNamedFieldChain s = new SameNamedFieldChain();
    s.other.count = 4;
    System.out.println(s.otherCount());
    System.out.println(s.count);
  }
}
