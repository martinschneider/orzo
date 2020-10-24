package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.cmp;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.cond;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Comparators;
import io.github.martinschneider.orzo.parser.productions.Condition;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConditionParserTest {
  private ConditionParser target = new ConditionParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("y=0", null),
        args("y==0", cond(expr("y"), cmp(Comparators.EQUAL), expr("0"))),
        args("x<=z", cond(expr("x"), cmp(Comparators.SMALLEREQ), expr("z"))),
        args("abc<xyz", cond(expr("abc"), cmp(Comparators.SMALLER), expr("xyz"))),
        args(
            "basketball > football",
            cond(expr("basketball"), cmp(Comparators.GREATER), expr("football"))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Condition expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
