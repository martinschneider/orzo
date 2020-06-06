package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.parser.TestHelper.arrSel;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.exp;
import static io.github.martinschneider.orzo.parser.TestHelper.id;
import static io.github.martinschneider.orzo.parser.TestHelper.pAssign;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.ParallelAssignment;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParallelAssignmentParserTest {
  private ParallelAssignmentParser target =
      new ParallelAssignmentParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("a=b", pAssign(List.of(id("a")), List.of(exp("b")))),
        Arguments.of(
            "a[1],b=b,c",
            pAssign(
                List.of(id("a", arrSel(List.of(exp("1")))), id("b")), List.of(exp("b"), exp("c")))),
        Arguments.of(
            "a,c=b[1],d", pAssign(List.of(id("a"), id("c")), List.of(exp("b[1]"), exp("d")))),
        Arguments.of(
            "array[left],array[right]=array[right],array[left]",
            pAssign(
                List.of(
                    id("array", arrSel(List.of(exp("left")))),
                    id("array", arrSel(List.of(exp("right"))))),
                List.of(exp("array[right]"), exp("array[left]")))),
        Arguments.of(
            "a,b=b+1,a+1", pAssign(List.of(id("a"), id("b")), List.of(exp("b+1"), exp("a+1")))),
        Arguments.of(
            "a,b,c=d,e,f",
            pAssign(List.of(id("a"), id("b"), id("c")), List.of(exp("d"), exp("e"), exp("f")))),
        Arguments.of(
            "a,b=test(b),a", pAssign(List.of(id("a"), id("b")), List.of(exp("test(b)"), exp("a")))),
        Arguments.of(
            "a,b,c=1+2*3/4%5>>6>>>7<<8,test(3*4),a+test(1/2)",
            pAssign(
                List.of(id("a"), id("b"), id("c")),
                List.of(exp("1+2*3/4%5>>6>>>7<<8"), exp("test(3*4)"), exp("a+test(1/2)")))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, ParallelAssignment expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
