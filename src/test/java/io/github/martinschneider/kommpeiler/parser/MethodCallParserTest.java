package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.arrSel;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.exp;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.id;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.methodCall;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MethodCallParserTest {
  private MethodCallParser target = new MethodCallParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("a", null),
        Arguments.of("a.b", null),
        Arguments.of(
            "calculateSomething(a[x])",
            methodCall(
                id("calculateSomething"),
                List.of(exp(List.of(id("a", arrSel(List.of(exp("x"))))))))),
        Arguments.of("calculateSomething()", methodCall(id("calculateSomething"), emptyList())),
        Arguments.of(
            "calculateSomething(x)", methodCall(id("calculateSomething"), List.of(exp("x")))),
        Arguments.of(
            "calculateSomething(a,b,c)",
            methodCall(id("calculateSomething"), List.of(exp("a"), exp("b"), exp("c")))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, MethodCall expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }

  private static Stream<Arguments> testArgs() throws IOException {
    return Stream.of(
        Arguments.of("()", Collections.emptyList()),
        Arguments.of("(x)", List.of(exp("x"))),
        Arguments.of("(ab,cd,efg)", List.of(exp("ab"), exp("cd"), exp("efg"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testArgs(String input, List<Expression> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parseArgs(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
