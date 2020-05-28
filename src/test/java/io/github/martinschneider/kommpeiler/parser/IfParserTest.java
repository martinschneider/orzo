package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assign;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.cond;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.ifBlk;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.ifStmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IfParserTest {
  private IfParser target = new IfParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of(
            "if (x==9){x=8}", ifStmt(List.of(ifBlk(cond("x==9"), List.of(assign("x=8")))), false)));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, IfStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
