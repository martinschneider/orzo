package io.github.martinschneider.orzo.tests;

public class ObjectUtilsUsage {
  public static void main(String[] args) {
    ObjectUtilsUsagePoint p1 = new ObjectUtilsUsagePoint(1, "a");
    ObjectUtilsUsagePoint p2 = new ObjectUtilsUsagePoint(1, "a");
    ObjectUtilsUsagePoint p3 = new ObjectUtilsUsagePoint(2, "b");
    System.out.println(p1.equals(p2));
    System.out.println(p1.equals(p3));
    int h1 = p1.hashCode();
    int h2 = p2.hashCode();
    System.out.println(h1 - h2);
    System.out.println(p1.toString());
  }
}
