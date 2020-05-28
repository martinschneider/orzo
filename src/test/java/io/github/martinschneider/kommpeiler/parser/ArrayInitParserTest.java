package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.arrInit;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.exp;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ArrayInitParserTest {
  private ArrayInitParser target = new ArrayInitParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("new int[]{1,2,3};", arrInit("int", 3, List.of(exp("1"), exp("2"), exp("3")))),
        Arguments.of("new int[]{1,2,3}", arrInit("int", 3, List.of(exp("1"), exp("2"), exp("3")))),
        Arguments.of(
            "new int[3]{1,2,3}", arrInit("int", 3, List.of(exp("1"), exp("2"), exp("3")))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Expression expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    Expression arrInit = target.parse(tokens);
    assertEquals(expected, arrInit);
    assertTokenIdx(tokens, (expected == null));
  }
}
