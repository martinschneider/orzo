package io.github.martinschneider.orzo.error;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.util.Factory.err;
import static io.github.martinschneider.orzo.util.Factory.list;
import static io.github.martinschneider.orzo.util.Factory.stream;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.ClassParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParserErrorTest {
  private ClassParser target = new ClassParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> testClass() throws IOException {
    return stream(
        args("public class Martin {", list(err("EOF parse class: expected RBRACE but found EOF"))),
        args(
            "public class Martin }",
            list(err("L1:21 parse class: expected LBRACE but found RBRACE"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testClass(String input, List<String> errors) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    target.parse(tokens);
    assertTrue(target.ctx.errors.errors.containsAll(errors), target.ctx.errors.errors.toString());
  }
}
