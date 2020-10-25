package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.arrInit;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ArrayInitParserTest {
  private ArrayInitParser target = new ArrayInitParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("new int[5]", arrInit("int", 5, emptyList())),
        args("new int[]{1,2,3}", arrInit("int", 3, list(expr("1"), expr("2"), expr("3")))),
        args("new int[3]{1,2,3}", arrInit("int", 3, list(expr("1"), expr("2"), expr("3")))),
        args("new byte[] {(byte)1}", arrInit("byte", 1, list(expr("(byte)1")))),
        args("new byte[a+b]", arrInit("byte", list(expr("a+b")), list(emptyList()))),
        args(
            "new byte[left.length + right.length]",
            arrInit("byte", list(expr("left.length+right.length)")), list(emptyList()))),
        args(
            "new byte[] {(byte) ((val >> 8) & 255)}",
            arrInit("byte", 1, list(expr("(byte) ((val >> 8) & 255)")))));
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
