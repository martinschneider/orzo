public class FloatCast {
  static double getDouble() {
    return 3.14;
  }

  public static float toFloat(double d) {
    return (float) d;
  }

  public static Object boxFloat(double d) {
    return Float.valueOf((float) d);
  }

  public static Object boxFromMethod() {
    return Float.valueOf((float) getDouble());
  }

  public static float castFromString(Object obj) {
    return (float) ((String) obj).length();
  }

  public static void main(String[] args) {
    System.out.println(toFloat(3.14));
    System.out.println(boxFloat(2.71));
    System.out.println(boxFromMethod());
    System.out.println(castFromString("hello"));
  }
}
