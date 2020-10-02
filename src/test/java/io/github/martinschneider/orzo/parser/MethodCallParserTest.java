package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.parser.TestHelper.arrInit;
import static io.github.martinschneider.orzo.parser.TestHelper.arrSel;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.id;
import static io.github.martinschneider.orzo.parser.TestHelper.methodCall;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
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
                "calculateSomething", List.of(expr(List.of(id("a", arrSel(List.of(expr("x")))))))),
            Arguments.of("calculateSomething()", methodCall("calculateSomething", emptyList())),
            Arguments.of(
                "calculateSomething(x)", methodCall("calculateSomething", List.of(expr("x")))),
            Arguments.of(
                "calculateSomething(a,b,c)",
                methodCall("calculateSomething", List.of(expr("a"), expr("b"), expr("c")))),
            Arguments.of("√(n)", methodCall("Math.sqrt", List.of(expr("n")))),
            Arguments.of("⌊x⌋", methodCall("Math.round", List.of(expr("Math.floor(x)")))),
            Arguments.of(
                "test(new int[] {1, 2})",
                methodCall("test", List.of(arrInit("int", 2, List.of(expr("1"), expr("2")))))),
            Arguments.of(
                "test(new int[] {1, 2}, new int[] {3, 4})",
                methodCall(
                    "test",
                    List.of(
                        arrInit("int", 2, List.of(expr("1"), expr("2"))),
                        arrInit("int", 2, List.of(expr("3"), expr("4"))))))));
    // Arguments.of(
    // "⌊((1+√5)/2) ** n)/√5+0.5⌋",
    // methodCall(id("Math.round"), List.of(expr("Math.floor(((1+√5)/2) **
    // n)/√5+0.5)")))));
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
        Arguments.of("(x)", List.of(expr("x"))),
        Arguments.of("(ab,cd,efg)", List.of(expr("ab"), expr("cd"), expr("efg"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testArgs(String input, List<Expression> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parseArgs(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
