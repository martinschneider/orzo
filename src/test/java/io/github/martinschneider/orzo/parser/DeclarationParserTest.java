package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.parser.TestHelper.arrInit;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.decl;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.pDecl;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
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
        Arguments.of("int x=100;", pDecl(null, id("x"), INT, expr("100"))),
        Arguments.of("boolean b=true;", pDecl(null, id("b"), "boolean", expr("true"))),
        Arguments.of("boolean bool=false;", pDecl(null, id("bool"), "boolean", expr("false"))),
        Arguments.of("String martin", pDecl(null, id("martin"), "String", null)),
        Arguments.of("double d=1.23", pDecl(null, id("d"), "double", expr("1.23"))),
        Arguments.of("int i=fac(100)", pDecl(null, id("i"), INT, expr("fac(100)"))),
        Arguments.of(
            "int[] a = new int[] {1, 2, 3};",
            pDecl(
                null, id("a"), INT, 1, arrInit(INT, 3, List.of(expr("1"), expr("2"), expr("3"))))),
        Arguments.of(
            "int[] a = new int[5];",
            pDecl(null, id("a"), INT, 1, arrInit(INT, List.of(5), emptyList()))),
        Arguments.of(
            "int a,b,c=1,2,3;",
            pDecl(
                List.of(
                    decl(null, id("a"), INT, expr("1")),
                    decl(null, id("b"), INT, expr("2")),
                    decl(null, id("c"), INT, expr("3"))))),
        Arguments.of(
            "double a,b,c=1,2;",
            pDecl(
                List.of(
                    decl(null, id("a"), DOUBLE, expr("1")),
                    decl(null, id("b"), DOUBLE, expr("2")),
                    decl(null, id("c"), DOUBLE, null)))));
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, ParallelDeclaration expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
