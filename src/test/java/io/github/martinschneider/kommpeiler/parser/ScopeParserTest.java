package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
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
