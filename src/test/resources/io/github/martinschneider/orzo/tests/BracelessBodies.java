package io.github.martinschneider.orzo.tests;

public class BracelessBodies {
  public static void main(String[] args) {
    String s = "[[Lx;I";
    int p = 0;
    while (p < s.length() && s.charAt(p) == '[') p++;
    System.out.println(p);
    if (s.charAt(p) == 'L') p = s.indexOf(';', p) + 1;
    else p++;
    System.out.println(p);
    int total = 0;
    for (int i = 0; i < 4; i++) total += i;
    System.out.println(total);
  }
}
