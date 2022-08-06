package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.lexer.tokens.Type;
import java.util.Map;

public class TypeMap {
  public final Map<String, Type> TYPES = Map.of("Object", new Type("java/lang/Object"));
}
