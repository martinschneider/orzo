public class StringLiteralMethod {
  public static boolean check(String type) {
    if ("long".equals(type)) {
      return true;
    }
    if ("float".equals(type)) {
      return true;
    }
    return false;
  }

  public static void main(String[] args) {
    System.out.println(check("long"));
    System.out.println(check("float"));
    System.out.println(check("int"));
  }
}
