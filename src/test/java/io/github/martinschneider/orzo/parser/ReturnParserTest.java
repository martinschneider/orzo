package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.exp;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReturnParserTest {
  private ReturnParser target = new ReturnParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("return a;", new ReturnStatement(exp("a"))),
        Arguments.of("return;", new ReturnStatement(null)));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, ReturnStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
