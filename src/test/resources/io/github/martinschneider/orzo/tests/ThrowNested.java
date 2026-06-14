public class ThrowNested {
  public static String parse(String val) {
    if (val.startsWith("0x") || val.startsWith("0X")) {
      String hexValue = val.substring(2);
      if (hexValue.isEmpty()) {
        throw new NumberFormatException("Invalid hexadecimal literal: " + val);
      }
      return "hex:" + hexValue;
    }
    return "dec:" + val;
  }

  public static void main(String[] args) {
    System.out.println(parse("0xFF"));
    System.out.println(parse("42"));
  }
}
