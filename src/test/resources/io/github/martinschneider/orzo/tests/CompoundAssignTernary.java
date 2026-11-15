package io.github.martinschneider.orzo.tests;

public class CompoundAssignTernary {
  public static void main(String[] args) {
    String a = "x";
    String x = "x";
    int count = 0;
    count += (a != null && a.equals(x)) ? 2 : 1;
    System.out.println(count); // 2

    String argType = "long";
    count += (argType != null && (argType.equals("long") || argType.equals("double"))) ? 2 : 1;
    System.out.println(count); // 4

    argType = "int";
    count += (argType != null && (argType.equals("long") || argType.equals("double"))) ? 2 : 1;
    System.out.println(count); // 5
  }
}
