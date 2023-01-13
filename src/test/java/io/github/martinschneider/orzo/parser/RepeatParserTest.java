package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.util.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
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
            "repeat(100){doSomething(i)}",
            forStmt(
                pDecl("int i=1"),
                expr("i<100"),
                assign("i++"),
                list(methodCall("doSomething", list(expr("i")))))),
        args(
            "repeat(n){doSomething(i)}",
            forStmt(
                pDecl("int i=1"),
                expr("i<n"),
                assign("i++"),
                list(methodCall("doSomething", list(expr("i")))))),
        args(
            "repeat n+1/3 {doSomething(i)}",
            forStmt(
                pDecl("int i=1"),
                expr("i<n+1/3"),
                assign("i++"),
                list(methodCall("doSomething", list(expr("i")))))),
        args(
            "repeat {doSomething(i)}",
            whileStmt(expr("true"), list(methodCall("doSomething", list(expr("i")))))));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, LoopStatement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, target.ctx.errors, (expected == null));
  }
}
