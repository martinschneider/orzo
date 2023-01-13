package io.github.martinschneider.orzo.parser;

import java.util.Map;

import io.github.martinschneider.orzo.parser.productions.Type;

public class TypeMap {
  public final Map<String, Type> TYPES = Map.of("Object", Type.of("java/lang/Object"));
}
