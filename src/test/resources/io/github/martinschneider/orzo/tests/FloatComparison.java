package io.github.martinschneider.orzo.tests;

public class FloatComparison {
  static int classifyFloat(float f) {
    if (f == 0) return 0;
    if (f == 1) return 1;
    if (f < 0) return -1;
    if (f > 1) return 2;
    return 3;
  }

  static int classifyDouble(double d) {
    if (d == 0) return 0;
    if (d == 1) return 1;
    if (d < 0) return -1;
    if (d > 1) return 2;
    return 3;
  }

  public static void main(String[] args) {
    float f0 = 0;
    float f1 = 1;
    float fm1 = -1;
    float f2 = 2;
    float fhalf = 0;
    fhalf = fhalf + 1;
    fhalf = fhalf / 2;
    System.out.println(classifyFloat(f0));
    System.out.println(classifyFloat(f1));
    System.out.println(classifyFloat(fm1));
    System.out.println(classifyFloat(f2));
    System.out.println(classifyFloat(fhalf));
    double d0 = 0;
    double d1 = 1;
    double dm1 = -1;
    double d2 = 2;
    System.out.println(classifyDouble(d0));
    System.out.println(classifyDouble(d1));
    System.out.println(classifyDouble(dm1));
    System.out.println(classifyDouble(d2));
  }
}
