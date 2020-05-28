package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assign;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.cond;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.exp;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.forStmt;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.methodCall;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.ForStatement;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ForParserTest {
  private ForParser target = new ForParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("for (){}", null),
        Arguments.of(
            "for (int i=1; i<100; i++){doSomething(i)}",
            forStmt(
                assign("int i=1"),
                cond("i<100"),
                assign("i++"),
                List.of(methodCall(id("doSomething"), List.of(exp("i")))))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, ForStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
