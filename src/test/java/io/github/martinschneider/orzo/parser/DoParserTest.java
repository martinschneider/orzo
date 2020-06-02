package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.assign;
import static io.github.martinschneider.orzo.parser.TestHelper.cond;
import static io.github.martinschneider.orzo.parser.TestHelper.doStmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DoParserTest {
  private DoParser target = new DoParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("do{x=x*3-1;}while(x>0);", doStmt(cond("x>0"), List.of(assign("x=x*3-1;")))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, DoStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    DoStatement doStatement = target.parse(tokens);
    assertEquals(expected, doStatement);
    assertTokenIdx(tokens, (expected == null));
  }
}
