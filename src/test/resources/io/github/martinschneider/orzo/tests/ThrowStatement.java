public class ThrowStatement {
  public static int parse(String s) {
    if (s.isEmpty()) {
      throw new RuntimeException("empty input");
    }
    return s.length();
  }

  public static String format(String prefix, String s) {
    if (s.isEmpty()) {
      throw new IllegalArgumentException("invalid: " + s);
    }
    return prefix + s;
  }

  public static void main(String[] args) {
    System.out.println(parse("hello"));
    System.out.println(format("ok-", "world"));
  }
}
