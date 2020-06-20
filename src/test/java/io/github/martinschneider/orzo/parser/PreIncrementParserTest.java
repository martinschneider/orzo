package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.parser.TestHelper.arrSel;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.id;
import static io.github.martinschneider.orzo.parser.TestHelper.inc;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Increment;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PreIncrementParserTest {
  private PreIncrementParser target =
      new PreIncrementParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("++i", inc(id("i"), op(PRE_INCREMENT))),
        Arguments.of("++abc", inc(id("abc"), op(PRE_INCREMENT))),
        Arguments.of("++a[0]", inc(id("a", arrSel(List.of(expr("0")))), op(PRE_INCREMENT))),
        Arguments.of("--i", inc(id("i"), op(PRE_DECREMENT))),
        Arguments.of("--abc", inc(id("abc"), op(PRE_DECREMENT))),
        Arguments.of("--a[0]", inc(id("a", arrSel(List.of(expr("0")))), op(PRE_DECREMENT))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Increment expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
