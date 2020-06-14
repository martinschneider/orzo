package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TypeUtilsTest {
  private static Stream<Arguments> testPermutations() {
    return Stream.of(
        Arguments.of(
            List.of(List.of(BYTE, SHORT, INT), List.of(LONG)),
            List.of(List.of(BYTE, LONG), List.of(SHORT, LONG), List.of(INT, LONG))));
  }

  @MethodSource
  @ParameterizedTest
  public void testPermutations(List<List<String>> input, List<List<String>> expected) {
    assertEquals(TypeUtils.combinations(input), expected);
  }
}
