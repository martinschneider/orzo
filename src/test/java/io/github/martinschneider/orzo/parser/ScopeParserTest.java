package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ScopeParserTest {
  private ScopeParser target = new ScopeParser();
  private Lexer lexer = new Lexer();

  private static Stream<Arguments> test() {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("public", scope(PUBLIC)),
        Arguments.of("private", scope(PRIVATE)),
        Arguments.of("protected", scope(PROTECTED)),
        Arguments.of("Public", null));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Scope expected) throws IOException {
    TokenList tokens = lexer.getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
