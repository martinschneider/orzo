package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.DIV;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MINUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MOD;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.PLUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.TIMES;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.integer;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.lexer.Lexer;
import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExpressionParserTest {
  // TODO: test these separately
  private ExpressionParser target = new ExpressionParser(ParserContext.build(new CompilerErrors()));
  private ExpressionParser2 target2;

  private static Stream<Arguments> test() {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("3+4", List.of(integer(3), integer(4), op(PLUS))),
        Arguments.of(
            "3+x*y-7",
            List.of(integer(3), id("x"), id("y"), op(TIMES), op(PLUS), integer(7), op(MINUS))),
        Arguments.of("-5*7+4", List.of(integer(-5), integer(7), op(TIMES), integer(4), op(PLUS))),
        Arguments.of(
            "x*3+7-8/1+0%6",
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
                op(PLUS))),
        Arguments.of("3+4", List.of(integer(3), integer(4), op(PLUS))),
        Arguments.of("5+7/2", List.of(integer(5), integer(7), integer(2), op(DIV), op(PLUS))),
        Arguments.of("(5+7)/2", List.of(integer(5), integer(7), op(PLUS), integer(2), op(DIV))));
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, List<Token> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    Expression expression = target.parse(tokens);
    CGContext cgContext = new CGContext();
    cgContext.parserCtx = ParserContext.build(new CompilerErrors());
    target2 = new ExpressionParser2(cgContext);
    List<Token> actual = (expression != null) ? target2.postfix(expression.getInfix()) : null;
    assertEquals(expected, actual);
    assertTokenIdx(tokens, (expected == null));
  }
}
