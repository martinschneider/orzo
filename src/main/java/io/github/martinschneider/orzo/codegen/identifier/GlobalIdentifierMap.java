package io.github.martinschneider.orzo.codegen.identifier;

import java.util.Map;

public class GlobalIdentifierMap {
  // fqn of the class
  public String key;

  public Map<String, IdentifierInfo> values;

  // TODO: this is temporary, merge this into values
  public IdentifierMap variables = new IdentifierMap();
}
