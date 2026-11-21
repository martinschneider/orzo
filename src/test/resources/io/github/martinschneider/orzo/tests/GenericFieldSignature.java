package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericFieldSignature {
  public List<String> items = new ArrayList<>();
  public static Map<String, byte[]> codes = new HashMap<>();

  public static void main(String[] args) throws Exception {
    // the fields' Signature attributes make their type arguments visible to reflection
    Class<?> c = GenericFieldSignature.class;
    System.out.println(c.getField("items").getGenericType().getTypeName());
    System.out.println(c.getField("codes").getGenericType().getTypeName());
  }
}
