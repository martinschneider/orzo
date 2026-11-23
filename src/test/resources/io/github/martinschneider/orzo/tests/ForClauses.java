package io.github.martinschneider.orzo.tests;

public class ForClauses {
  // several updates, empty conditions and updates, several declarators
  public static void main(String[] args) {
    int s = 0;
    for (int i = 0, j = 10; i < j; i++, j--) {
      s += j - i;
    }
    int k;
    for (k = 0; k < 3; k++, s++) {}
    for (; ; ) {
      if (++s > 40) {
        break;
      }
    }
    for (int i = 0; i < 3; ) {
      i++;
      s += i;
    }
    int a = 1, b = 2, c;
    c = a + b;
    System.out.println(s + " " + k + " " + c);
  }
}
