package io.github.martinschneider.orzo.tests;

// local variables declared with "var" (also in for loops and lambda parameters)
import java.util.*;
import java.util.function.*;

public class VarDeclarations {
  static String name() { return "n"; }
  public static void main(String[] args) {
    var i = 1;
    var l = 2L + i;
    var d = 1.5 * i;
    var s = "a" + i;
    var list = new ArrayList<String>();
    list.add("x");
    list.add(s);
    var arr = new int[] {1, 2, 3};
    var names = new String[2];
    names[0] = name();
    var c = 'c';
    var b = i > 0;
    var o = new Object();
    var sb = new StringBuilder();
    sb.append(l).append(d);
    for (var k = 0; k < 2; k++) sb.append(k);
    for (var e : arr) sb.append(e);
    for (var x : list) sb.append(x.length());
    BiFunction<Integer, Integer, Integer> add = (var x, var y) -> x + y;
    var m = new HashMap<String, Integer>();
    m.put("k", 1);
    var var = 3;
    System.out.println(i + " " + l + " " + d + " " + s + " " + list + " " + arr.length + " " + names[0] + c + b + (o != null) + sb + add.apply(2, 3) + m + var);
    VarLoops.run();
  }
}
class VarLoops {
  static void run() {
    var m = new java.util.TreeMap<String, Integer>();
    m.put("a", 1);
    m.put("bb", 2);
    for (var e : m.entrySet()) System.out.println(e.getKey().length() + e.getValue());
    var keys = m.keySet();
    for (var k : keys) System.out.println(k.toUpperCase());
    var fields = "run.it".split("[.]");
    for (var f : fields) System.out.println(f.length());
    var n = new int[] {7};
    for (var x : n) System.out.println(x + 1);
    var copy = keys;
    for (var k : copy) System.out.println(k.charAt(0));
    for (var o : java.util.List.of(1, 2)) System.out.println(o);
  }
}
