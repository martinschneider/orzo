package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.arrSel;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.id;
import static io.github.martinschneider.orzo.parser.TestHelper.inc;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PostIncrementParserTest {
  private PostIncrementParser target =
      new PostIncrementParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("i++", inc(id("i"), op(POST_INCREMENT))),
        args("abc++", inc(id("abc"), op(POST_INCREMENT))),
        args("a[0]++", inc(id("a", arrSel(list(expr("0")))), op(POST_INCREMENT))),
        args("i--", inc(id("i"), op(POST_DECREMENT))),
        args("abc--", inc(id("abc"), op(POST_DECREMENT))),
        args("a[0]--", inc(id("a", arrSel(list(expr("0")))), op(POST_DECREMENT))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, IncrementStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
