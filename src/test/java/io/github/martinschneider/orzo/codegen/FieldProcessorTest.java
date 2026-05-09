package io.github.martinschneider.orzo.codegen;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class FieldProcessorTest {
  // Regression test: subclasses accessing inherited fields from a parent class not in allClazzes
  // (e.g. Str extends Token) must find those fields via reflection fallback.
  @Test
  public void testGetInstanceFieldMapReflectionFallback() {
    FieldProcessor fp = new FieldProcessor();
    // Token has public Object val - not in allClazzes, so reflection must kick in
    Map<String, FieldProcessor.InstanceField> fields =
        fp.getInstanceFieldMap("io.github.martinschneider.orzo.lexer.tokens.Token", emptyList());
    assertTrue(fields.containsKey("val"), "Expected inherited field 'val' via reflection");
    assertEquals("java.lang.Object", fields.get("val").fieldType);
  }
}
