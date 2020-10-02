package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POW;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Token.fp;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.id;
import static io.github.martinschneider.orzo.parser.TestHelper.methodCall;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExpressionParserTest {
  private ExpressionParser target = new ExpressionParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("3+4", expr(List.of(integer(3), integer(4), op(PLUS)))),
        Arguments.of(
            "3+x*y-7",
            expr(
                List.of(integer(3), id("x"), id("y"), op(TIMES), op(PLUS), integer(7), op(MINUS)))),
        Arguments.of(
            "-5*7+4", expr(List.of(integer(-5), integer(7), op(TIMES), integer(4), op(PLUS)))),
        Arguments.of(
            "x*3+7-8/1+0%6",
            expr(
                List.of(
                    id("x"),
                    integer(3),
                    op(TIMES),
                    integer(7),
                    op(PLUS),
                    integer(8),
                    integer(1),
                    op(DIV),
                    op(MINUS),
                    integer(0),
                    integer(6),
                    op(MOD),
                    op(PLUS)))),
        Arguments.of("3+4", expr(List.of(integer(3), integer(4), op(PLUS)))),
        Arguments.of("5+7/2", expr(List.of(integer(5), integer(7), integer(2), op(DIV), op(PLUS)))),
        Arguments.of(
            "(5+7)/2", expr(List.of(integer(5), integer(7), op(PLUS), integer(2), op(DIV)))),
        Arguments.of("√5", expr(List.of(fp(2.23606797749979)))),
        Arguments.of("√n", expr(List.of(methodCall("Math.sqrt", List.of(expr("n")))))),
        Arguments.of("√(n)", expr(List.of(methodCall("Math.sqrt", List.of(expr("n")))))),
        Arguments.of("1 ** 2", expr(List.of(integer(1), integer(2), op(POW)))),
        Arguments.of("-1 ** 2", expr(List.of(integer(-1), integer(2), op(POW)))),
        Arguments.of("-1 ** -2", expr(List.of(integer(-1), integer(-2), op(POW)))),
        Arguments.of("-1 * -2", expr(List.of(integer(-1), integer(-2), op(TIMES)))),
        Arguments.of("-1 / -2", expr(List.of(integer(-1), integer(-2), op(DIV)))),
        Arguments.of("-1 % -2", expr(List.of(integer(-1), integer(-2), op(MOD)))),
        Arguments.of("-1 - (-2)", expr(List.of(integer(-1), integer(-2), op(MINUS)))),
        Arguments.of("1 ** i", expr(List.of(integer(1), id("i"), op(POW)))),
        Arguments.of("-1 ** i", expr(List.of(integer(-1), id("i"), op(POW)))),
        Arguments.of(
            "-1 ** -i", expr(List.of(integer(-1), integer(-1), id("i"), op(TIMES), op(POW)))),
        Arguments.of(
            "-1 * -i", expr(List.of(integer(-1), integer(-1), id("i"), op(TIMES), op(TIMES)))),
        Arguments.of(
            "-1 / -i", expr(List.of(integer(-1), integer(-1), id("i"), op(TIMES), op(DIV)))),
        Arguments.of(
            "-1 % -i", expr(List.of(integer(-1), integer(-1), id("i"), op(TIMES), op(MOD)))),
        Arguments.of(
            "-1 - (-i)", expr(List.of(integer(-1), integer(-1), id("i"), op(TIMES), op(MINUS)))),
        Arguments.of("(byte) 1", expr(List.of(integer(1)), Type.type("byte"))));
        //Arguments.of("a.b", expr(List.of(id("a", "b"))))
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, Expression expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
