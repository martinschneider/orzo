package io.github.martinschneider.orzo.codegen;

import java.util.HashMap;
import java.util.Map;

public class ClassMembers {
  private Map<String, String> methods = new HashMap<>();
  private Map<String, String> fields = new HashMap<>();

  public void addMethod(String key, String member) {
    methods.put(key, member);
  }

  public void addField(String key, String member) {
    fields.put(key, member);
  }
}
