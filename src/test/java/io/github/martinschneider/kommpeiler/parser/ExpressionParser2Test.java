package io.github.martinschneider.kommpeiler.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExpressionParser2Test {
  private ExpressionParser2 target;

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of(
            List.of(
                new IntNum(5),
                new Operator(Operators.PLUS),
                new IntNum(7),
                new Operator(Operators.DIV),
                new IntNum(2)),
            List.of(
                new IntNum(5),
                new IntNum(7),
                new IntNum(2),
                new Operator(Operators.DIV),
                new Operator(Operators.PLUS))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(List<Token> input, List<Token> expectedOutput) {
    CGContext cgContext = new CGContext();
    cgContext.parserCtx = ParserContext.build(new CompilerErrors());
    target = new ExpressionParser2(cgContext);
    assertEquals(expectedOutput, target.postfix(input));
  }
}
