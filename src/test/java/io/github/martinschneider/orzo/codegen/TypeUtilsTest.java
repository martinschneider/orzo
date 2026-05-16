package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static io.github.martinschneider.orzo.util.FactoryHelper.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TypeUtilsTest {
  private static Stream<Arguments> testPermutations() {
    return stream(
        args(
            list(list(BYTE, SHORT, INT), list(LONG)),
            list(list(BYTE, LONG), list(SHORT, LONG), list(INT, LONG))));
  }

  @MethodSource
  @ParameterizedTest
  public void testPermutations(List<List<String>> input, List<List<String>> expected) {
    assertEquals(TypeUtils.combinations(input), expected);
  }

  private static Stream<Arguments> testDescr() {
    return stream(
        // primitives
        args("int", "I"),
        args("byte", "B"),
        args("char", "C"),
        args("double", "D"),
        args("float", "F"),
        args("long", "J"),
        args("short", "S"),
        args("void", "V"),
        args("boolean", "Z"),
        // reference types whose names contain primitive substrings
        args("java.io.PrintStream", "Ljava/io/PrintStream;"),
        args("java.io.InputStream", "Ljava/io/InputStream;"),
        // String shorthands
        args("String", "Ljava/lang/String;"),
        args("java.lang.String", "Ljava/lang/String;"),
        // class names that contain primitive/type substrings — must not be mangled
        args("java.lang.StringBuilder", "Ljava/lang/StringBuilder;"),
        // already-formed descriptors pass through
        args("Ljava/lang/Object;", "Ljava/lang/Object;"),
        args("[I", "[I"),
        args("[Ljava/lang/Object;", "[Ljava/lang/Object;"));
  }

  @MethodSource
  @ParameterizedTest
  public void testDescr(String type, String expected) {
    assertEquals(expected, TypeUtils.descr(type));
  }

  private static Stream<Arguments> testDescrArray() {
    return stream(
        args("int", 1, "[I"),
        args("byte", 1, "[B"),
        args("java.io.PrintStream", 1, "[Ljava/io/PrintStream;"),
        args("java.lang.String", 1, "[Ljava/lang/String;"));
  }

  @MethodSource
  @ParameterizedTest
  public void testDescrArray(String type, int arrDim, String expected) {
    assertEquals(expected, TypeUtils.descr(type, arrDim));
  }
}
