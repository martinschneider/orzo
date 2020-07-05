package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.type;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.assign;
import static io.github.martinschneider.orzo.parser.TestHelper.clazz;
import static io.github.martinschneider.orzo.parser.TestHelper.method;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ClassParserTest {
  private ClassParser target = new ClassParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> testClass() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of(
            "public class Martin{public void test(){x=0;}}",
            clazz(
                null,
                emptyList(),
                scope(PUBLIC),
                id("Martin"),
                List.of(
                    method(
                        scope(PUBLIC),
                        type(VOID).toString(),
                        id("test"),
                        emptyList(),
                        List.of(assign("x=0")))),
                emptyList())),
        Arguments.of(
            "private class Laura{}",
            clazz(null, emptyList(), scope(PRIVATE), id("Laura"), emptyList(), emptyList())),
        Arguments.of(
            "class Empty{}",
            clazz(null, emptyList(), null, id("Empty"), emptyList(), emptyList())));
  }

  @ParameterizedTest
  @MethodSource
  public void testClass(String input, Clazz expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
