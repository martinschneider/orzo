package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.arrSel;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.exp;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.lexer.Lexer;
import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.parser.productions.ArraySelector;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ArraySelectorParserTest {
  private ArraySelectorParser target =
      new ArraySelectorParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("()", null),
        Arguments.of("[1]", arrSel(List.of(exp("1")))),
        Arguments.of(
            "[1][2+3][3*4][test]", arrSel(List.of(exp("1"), exp("2+3"), exp("3*4"), exp("test")))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, ArraySelector expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
