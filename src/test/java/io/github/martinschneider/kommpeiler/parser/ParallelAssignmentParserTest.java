package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.exp;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.pAssign;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
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
