package io.github.martinschneider.orzo.tests;

public class MidExpressionCasts { public static void main(String[] a) { char c = 'c'; long l = 300L; double d = 2.7; int x = 5; boolean ne = c != (char) -1; System.out.println("c " + (int) c + " " + (byte) l + " " + (x + (int) d) + " " + ne + " " + (x * (long) (x + 1)) + " " + (char) (c + 1)); int k = 0; String s = "a b"; for (int j = 0; j < s.length(); j++) { char ch = s.charAt(j); if (ch != (char) -1 && !Character.isWhitespace(ch)) { k++; } } System.out.println(k); } }
