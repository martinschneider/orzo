package io.github.martinschneider.orzo.tests;

public class Chars2 {
  public static void main(String[] args) {
    char[] c = new char[] {'a', 98, 'c', 100, 'e', 102, 'g', 104, 'i', 106, 'k', 'l', 109, 'n', 111,
        'p', 113, 'r', 115, 't', 117, 'v', 119, 120, 'y', 'z'};
    for (int i = 0; i < 26; i++) {
      System.out.println(c[i]);
    }
    char a = '('; // 40
    char b = '2'; // 50
    System.out.println(a + b); // 90 = 'Z'
  }
}
