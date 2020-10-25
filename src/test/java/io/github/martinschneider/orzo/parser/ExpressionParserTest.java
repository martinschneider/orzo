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
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.id;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.methodCall;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExpressionParserTest {
  private ExpressionParser target = new ExpressionParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("3+4", expr(list(integer(3), integer(4), op(PLUS)))),
        args(
            "3+x*y-7",
            expr(list(integer(3), id("x"), id("y"), op(TIMES), op(PLUS), integer(7), op(MINUS)))),
        args("-5*7+4", expr(list(integer(-5), integer(7), op(TIMES), integer(4), op(PLUS)))),
        args(
            "x*3+7-8/1+0%6",
            expr(
                list(
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
        args("3+4", expr(list(integer(3), integer(4), op(PLUS)))),
        args("5+7/2", expr(list(integer(5), integer(7), integer(2), op(DIV), op(PLUS)))),
        args("(5+7)/2", expr(list(integer(5), integer(7), op(PLUS), integer(2), op(DIV)))),
        args("√5", expr(list(fp(2.23606797749979)))),
        args("√n", expr(list(methodCall("Math.sqrt", list(expr("n")))))),
        args("√(n)", expr(list(methodCall("Math.sqrt", list(expr("n")))))),
        args("1 ** 2", expr(list(integer(1), integer(2), op(POW)))),
        args("-1 ** 2", expr(list(integer(-1), integer(2), op(POW)))),
        args("-1 ** -2", expr(list(integer(-1), integer(-2), op(POW)))),
        args("-1 * -2", expr(list(integer(-1), integer(-2), op(TIMES)))),
        args("-1 / -2", expr(list(integer(-1), integer(-2), op(DIV)))),
        args("-1 % -2", expr(list(integer(-1), integer(-2), op(MOD)))),
        args("-1 - (-2)", expr(list(integer(-1), integer(-2), op(MINUS)))),
        args("1 ** i", expr(list(integer(1), id("i"), op(POW)))),
        args("-1 ** i", expr(list(integer(-1), id("i"), op(POW)))),
        args("-1 ** -i", expr(list(integer(-1), integer(-1), id("i"), op(TIMES), op(POW)))),
        args("-1 * -i", expr(list(integer(-1), integer(-1), id("i"), op(TIMES), op(TIMES)))),
        args("-1 / -i", expr(list(integer(-1), integer(-1), id("i"), op(TIMES), op(DIV)))),
        args("-1 % -i", expr(list(integer(-1), integer(-1), id("i"), op(TIMES), op(MOD)))),
        args("-1 - (-i)", expr(list(integer(-1), integer(-1), id("i"), op(TIMES), op(MINUS)))),
        args("(byte) 1", expr(list(integer(1)), Type.type("byte"))),
        args("a.b", expr(list(id("a", "b")))),
        args("a.b.c.d", expr(list(id("a", "b", "c", "d")))));
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, Expression expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
