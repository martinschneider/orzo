package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.util.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DoParserTest {
  private DoParser target = new DoParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("do{x=x*3-1;}while(x>0);", doStmt(expr("x>0"), list(assign("x=x*3-1;")))));
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
