package io.github.martinschneider.orzo.tests;

public class PowerOperator { public static void main(String[] a) { int n = 3; double d = 1.5; long b = 2 ** 3; System.out.println((2 ** 3) + " " + (n ** 2) + " " + (2 ** n) + " " + (d ** 2) + " " + ((n + 1) ** 2) + " " + (n ** 2 + 1) + " " + b + " " + (2 ** 62)); double x = 2 ** 0.5; System.out.println(x); } }
