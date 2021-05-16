package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.util.FactoryHelper.*;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MethodCallParserTest {
  private MethodCallParser target = new MethodCallParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("a", null),
        args("a.b", null),
        args(
            "calculateSomething(a[x])",
            methodCall("calculateSomething", list(expr(list(id("a", arrSel(list(expr("x"))))))))),
        args("calculateSomething()", methodCall("calculateSomething", emptyList())),
        args("calculateSomething(x)", methodCall("calculateSomething", list(expr("x")))),
        args(
            "calculateSomething(a,b,c)",
            methodCall("calculateSomething", list(expr("a"), expr("b"), expr("c")))),
        args("√(n)", methodCall("Math.sqrt", list(expr("n")))),
        args("⌊x⌋", methodCall("Math.round", list(expr("Math.floor(x)")))),
        args(
            "test(new int[] {1, 2})",
            methodCall("test", list(arrInit("int", 2, list(expr("1"), expr("2")))))),
        args(
            "test(new int[] {1, 2}, new int[] {3, 4})",
            methodCall(
                "test",
                list(
                    arrInit("int", 2, list(expr("1"), expr("2"))),
                    arrInit("int", 2, list(expr("3"), expr("4")))))),
        args("doSomething()[0]", methodCall("doSomething", emptyList(), arrSel(list(expr("0"))))),
        args("someMethod(", null),
        args("someMethod(a", null));
    // args(
    // "⌊((1+√5)/2) ** n)/√5+0.5⌋",
    // methodCall(id("Math.round"), list(expr("Math.floor(((1+√5)/2) **
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
    return stream(
        args("()", emptyList()),
        args("(x)", list(expr("x"))),
        args("(ab,cd,efg)", list(expr("ab"), expr("cd"), expr("efg"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testArgs(String input, List<Expression> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parseArgs(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
