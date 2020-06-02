package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.assign;
import static io.github.martinschneider.orzo.parser.TestHelper.cond;
import static io.github.martinschneider.orzo.parser.TestHelper.whileStmt;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WhileParserTest {
  private WhileParser target = new WhileParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("while (){}", null),
        Arguments.of("while (1==1){}", whileStmt(cond("1==1"), emptyList())),
        Arguments.of(
            "while (x==9){x=8;y=7}",
            whileStmt(cond("x==9"), List.of(assign("x=8"), assign("y=7")))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, WhileStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
