package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
public class UnboxedOperands { public static void main(String[] a) { List<Integer> l = new ArrayList<>(); l.add(0); l.add(5); l.add(9); int start = 6; int line = l.size() - 1; while (l.get(line) > start) { line--; } boolean b = l.get(2) == 9; if (l.get(0) < 1) { System.out.println("lt"); } System.out.println(line + " " + b + " " + (l.get(1) + 1)); } }
