package io.github.martinschneider.orzo.tests;

public class EnumUsage {
  static String check(EnumUsageColor c) {
    return "c=" + c;
  }
  public static void main(String[] args) {
    EnumUsageColor[] colors = new EnumUsageColor[] {EnumUsageColor.RED, EnumUsageColor.GREEN};
    System.out.println(EnumUsageColor.RED.name() + " " + colors[1].name() + " " + check(EnumUsageColor.RED) + " " + EnumUsageColor.GREEN.ordinal() + " " + EnumUsageColor.valueOf("GREEN"));
  }
}
