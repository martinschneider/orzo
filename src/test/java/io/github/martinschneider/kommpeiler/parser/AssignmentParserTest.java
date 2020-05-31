package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.arrSel;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assign;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.exp;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
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
        Arguments.of("x=5", assign(id("x"), exp("5"))),
        Arguments.of("i++", assign(id("i"), exp("i++"))),
        Arguments.of("x=5*12-3/6+12%4", assign(id("x"), exp("5*12-3/6+12%4"))),
        Arguments.of("x[1]=3", assign(id("x", arrSel(List.of(exp("1")))), exp("3"))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Assignment expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}