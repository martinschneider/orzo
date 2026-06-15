import java.util.Collections;

public class StaticFieldMethodCall {
  public static void main(String[] args) {
    System.out.println(Collections.EMPTY_LIST.isEmpty());
    System.out.println(Collections.EMPTY_LIST.size());
  }
}
