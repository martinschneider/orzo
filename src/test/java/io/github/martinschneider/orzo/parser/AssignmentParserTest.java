package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.util.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AssignmentParserTest {
  private AssignmentParser target = new AssignmentParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("x>5", null),
        args("x=5", assign(list(id("x")), list(expr("5")))),
        args("x=true", assign(id("x"), expr("true"))),
        args("x=false", assign(id("x"), expr("false"))),
        args("x=5*12-3/6+12%4", assign(id("x"), expr("5*12-3/6+12%4"))),
        args("x[1]=3", assign(id("x", arrSel(list(expr("1")))), expr("3"))),
        args("this.a=1", assign(list(id("this", "a")), list(expr("1")))),
        args("this.a=a", assign(list(id("this", "a")), list(expr("a")))),
        args("this.a=b[1]", assign(list(id("this", "a")), list(expr("b[1]")))),
        args(
            "a[1],b=b,c",
            assign(list(id("a", arrSel(list(expr("1")))), id("b")), list(expr("b"), expr("c")))),
        args("a,c=b[1],d", assign(list(id("a"), id("c")), list(expr("b[1]"), expr("d")))),
        args(
            "array[left],array[right]=array[right],array[left]",
            assign(
                list(
                    id("array", arrSel(list(expr("left")))),
                    id("array", arrSel(list(expr("right"))))),
                list(expr("array[right]"), expr("array[left]")))),
        args("a,b=b+1,a+1", assign(list(id("a"), id("b")), list(expr("b+1"), expr("a+1")))),
        args(
            "a,b,c=d,e,f",
            assign(list(id("a"), id("b"), id("c")), list(expr("d"), expr("e"), expr("f")))),
        args("a,b=test(b),a", assign(list(id("a"), id("b")), list(expr("test(b)"), expr("a")))),
        args(
            "a,b,c=1+2*3/4%5>>6>>>7<<8,test(3*4),a+test(1/2)",
            assign(
                list(id("a"), id("b"), id("c")),
                list(expr("1+2*3/4%5>>6>>>7<<8"), expr("test(3*4)"), expr("a+test(1/2)")))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Assignment expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
