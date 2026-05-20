public class Switch {
  public static String classify(String s) {
    switch (s) {
      case "a":
      case "e":
      case "i":
      case "o":
      case "u":
        return "vowel";
      case "y":
        return "sometimes";
      default:
        return "consonant";
    }
  }

  public static String describe(String type) {
    switch (type) {
      case "int":
        return "integer";
      case "double":
        return "floating point";
      case "boolean":
        return "flag";
      default:
        return "other";
    }
  }

  public static String dayType(int day) {
    switch (day) {
      case 1:
      case 7:
        return "weekend";
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
        return "weekday";
      default:
        return "invalid";
    }
  }

  public static void main(String[] args) {
    System.out.println(classify("a"));
    System.out.println(classify("b"));
    System.out.println(classify("y"));
    System.out.println(describe("int"));
    System.out.println(describe("boolean"));
    System.out.println(describe("String"));
    int x = 0;
    switch (classify("e")) {
      case "vowel":
        x = 1;
        break;
      case "consonant":
        x = 2;
        break;
    }
    System.out.println(x);
    System.out.println(dayType(1));
    System.out.println(dayType(3));
    System.out.println(dayType(9));
  }
}
