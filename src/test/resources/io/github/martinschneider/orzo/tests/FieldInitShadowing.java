public class FieldInitShadowing {
  public String value = "initial";

  public FieldInitShadowing(String value) {
    this.value = value;
  }

  public static void main(String[] args) {
    FieldInitShadowing a = new FieldInitShadowing("hello");
    System.out.println(a.value);
    FieldInitShadowing b = new FieldInitShadowing("world");
    System.out.println(b.value);
  }
}
