public class ReturnNull {
  public static Object maybeNull(boolean flag) {
    if (flag) {
      return "yes";
    }
    return null;
  }

  public static void main(String[] args) {
    System.out.println(maybeNull(true));
    System.out.println(maybeNull(false));
  }
}
