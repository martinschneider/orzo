package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.util.FactoryHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import io.github.martinschneider.orzo.parser.productions.LoopStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class RepeatParserTest {
  private RepeatParser target = new RepeatParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("repeat (){}", null),
        args(
            "repeat(100){doSomething(x)}",
            forStmt(
                pDecl("int $repeat0=0"),
                expr("$repeat0<100"),
                new IncrementStatement(expr("$repeat0++")),
                list(methodCall("doSomething", list(expr("x")))))),
        args(
            "repeat(n){doSomething(x)}",
            forStmt(
                pDecl("int $repeat0=0"),
                expr("$repeat0<n"),
                new IncrementStatement(expr("$repeat0++")),
                list(methodCall("doSomething", list(expr("x")))))),
        args(
            "repeat n+1/3 {doSomething(x)}",
            forStmt(
                pDecl("int $repeat0=0"),
                expr("$repeat0<n+1/3"),
                new IncrementStatement(expr("$repeat0++")),
                list(methodCall("doSomething", list(expr("x")))))),
        args(
            "repeat {doSomething(x)}",
            whileStmt(expr("true"), list(methodCall("doSomething", list(expr("x")))))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, LoopStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, target.ctx.errors, (expected == null));
  }
}
