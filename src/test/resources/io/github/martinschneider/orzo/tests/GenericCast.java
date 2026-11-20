package io.github.martinschneider.orzo.tests;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericCast {
  public static void main(String[] args) {
    Object o = String.class;
    Class<?> c = (Class<?>) o;
    System.out.println(c.getSimpleName());
    System.out.println(((Class<?>) o).getName().length() + 1);
    Object m = new HashMap<String, List<String>>();
    Map<String, List<String>> map = (Map<String, List<String>>) m;
    map.put("k", new ArrayList<>());
    map.get("k").add("v");
    System.out.println(map.get("k").get(0));
    Class<?> mapClass = HashMap.class;
    TypeVariable<?>[] params = mapClass.getTypeParameters();
    System.out.println(params.length);
  }
}
