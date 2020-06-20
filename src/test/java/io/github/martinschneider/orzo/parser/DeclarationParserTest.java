package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.parser.TestHelper.arrInit;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.decl;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DeclarationParserTest {
  private DeclarationParser target =
      new DeclarationParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("int x=100;", decl(id("x"), "int", expr("100"), true)),
        Arguments.of("String martin", decl(id("martin"), "String", null, false)),
        Arguments.of("double d=1.23", decl(id("d"), "double", expr("1.23"), true)),
        Arguments.of("int i=fac(100)", decl(id("i"), "int", expr("fac(100)"), true)),
        Arguments.of(
            "int[] a = new int[] {1, 2, 3};",
            decl(
                id("a"),
                "int",
                1,
                arrInit("int", 3, List.of(expr("1"), expr("2"), expr("3"))),
                true)),
        Arguments.of(
            "int[] a = new int[5];",
            decl(id("a"), "int", 1, arrInit("int", List.of(5), emptyList()), false)));
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, Declaration expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
