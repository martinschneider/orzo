public class FloatCast {
  public static float toFloat(double d) {
    return (float) d;
  }

  public static Object boxFloat(double d) {
    return Float.valueOf((float) d);
  }

  public static void main(String[] args) {
    System.out.println(toFloat(3.14));
    System.out.println(boxFloat(2.71));
  }
}
