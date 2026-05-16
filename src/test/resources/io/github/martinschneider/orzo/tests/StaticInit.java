public class StaticInit {
  static int x;
  static String s;

  static {
    x = 42;
    s = "hello";
  }

  public static void main(String[] args) {
    System.out.println(x);
    System.out.println(s);
  }
}
