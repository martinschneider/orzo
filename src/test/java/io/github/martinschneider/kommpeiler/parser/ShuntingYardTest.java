package io.github.martinschneider.kommpeiler.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ShuntingYardTest {
  private ShuntingYard target = new ShuntingYard();

  @Test
  public void testExpressionParsing() {
    List<Token> input =
        List.of(
            new IntNum(5),
            new Operator(Operators.PLUS),
            new IntNum(7),
            new Operator(Operators.DIV),
            new IntNum(2));
    List<Token> output =
        List.of(
            new IntNum(5),
            new IntNum(7),
            new IntNum(2),
            new Operator(Operators.DIV),
            new Operator(Operators.PLUS));
    assertEquals(target.postfix(input), output);
  }
}
