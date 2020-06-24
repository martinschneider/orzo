package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.parser.TestHelper.arrSel;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.assign;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AssignmentParserTest {
  private AssignmentParser target = new AssignmentParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("x>5", null),
        Arguments.of("x=5", assign(List.of(id("x")), List.of(expr("5")))),
        Arguments.of("x=true", assign(id("x"), expr("true"))),
        Arguments.of("x=false", assign(id("x"), expr("false"))),
        Arguments.of("x=5*12-3/6+12%4", assign(id("x"), expr("5*12-3/6+12%4"))),
        Arguments.of(
            "x[1]=3",
            assign(id("x", arrSel(List.of(expr("1")))), expr("3")),
            Arguments.of(
                "a[1],b=b,c",
                assign(
                    List.of(id("a", arrSel(List.of(expr("1")))), id("b")),
                    List.of(expr("b"), expr("c")))),
            Arguments.of(
                "a,c=b[1],d", assign(List.of(id("a"), id("c")), List.of(expr("b[1]"), expr("d")))),
            Arguments.of(
                "array[left],array[right]=array[right],array[left]",
                assign(
                    List.of(
                        id("array", arrSel(List.of(expr("left")))),
                        id("array", arrSel(List.of(expr("right"))))),
                    List.of(expr("array[right]"), expr("array[left]")))),
            Arguments.of(
                "a,b=b+1,a+1",
                assign(List.of(id("a"), id("b")), List.of(expr("b+1"), expr("a+1")))),
            Arguments.of(
                "a,b,c=d,e,f",
                assign(
                    List.of(id("a"), id("b"), id("c")), List.of(expr("d"), expr("e"), expr("f")))),
            Arguments.of(
                "a,b=test(b),a",
                assign(List.of(id("a"), id("b")), List.of(expr("test(b)"), expr("a")))),
            Arguments.of(
                "a,b,c=1+2*3/4%5>>6>>>7<<8,test(3*4),a+test(1/2)",
                assign(
                    List.of(id("a"), id("b"), id("c")),
                    List.of(
                        expr("1+2*3/4%5>>6>>>7<<8"), expr("test(3*4)"), expr("a+test(1/2)"))))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Assignment expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
