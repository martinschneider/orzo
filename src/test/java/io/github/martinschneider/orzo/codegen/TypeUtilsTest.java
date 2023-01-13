package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.util.Factory.list;
import static io.github.martinschneider.orzo.util.Factory.stream;
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
}
