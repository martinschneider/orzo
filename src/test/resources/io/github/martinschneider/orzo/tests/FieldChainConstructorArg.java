package io.github.martinschneider.orzo.tests;

public class FieldChainConstructorArg {
  private GenericChainHolder holder = new GenericChainHolder();

  StringBuilder build(Object curr) {
    StringBuilder result = null;
    if (curr instanceof Integer) {
      result = new StringBuilder("int");
    } else if (curr instanceof String && holder.items.contains(curr)) {
      // a call chain starting at a field, as a constructor argument
      result = new StringBuilder(holder.items.get(0));
    }
    return result;
  }

  public static void main(String[] args) {
    FieldChainConstructorArg f = new FieldChainConstructorArg();
    f.holder.items.add("abc");
    System.out.println(f.build("abc"));
    System.out.println(f.build(1));
  }
}
