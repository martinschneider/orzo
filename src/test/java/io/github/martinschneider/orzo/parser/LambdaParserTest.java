package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.util.FactoryHelper.expr;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static io.github.martinschneider.orzo.util.FactoryHelper.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.LambdaExpr;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LambdaParserTest {
  private LambdaParser target = new LambdaParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("x + 1", null),
        args("x -> x.toString()", expr(list(new LambdaExpr(list("x"), expr("x.toString()"))))),
        args("(a, b) -> a", expr(list(new LambdaExpr(list("a", "b"), expr("a"))))),
        args("() -> 1", expr(list(new LambdaExpr(list(), expr("1"))))),
        // method references with type arguments (erased) and on array types
        args("Box<String>::getX", expr(list(LambdaExpr.methodRef("Box", "getX")))),
        args("int[]::new", expr(list(LambdaExpr.methodRef("int[]", "new")))),
        // Not a valid untyped lambda param list - "a.b" is a dotted identifier chain, not a bare
        // name - so this isn't a lambda at all; must leave `tokens` untouched.
        args("(a.b, c) -> a", null));
  }

  // (the declared parameter types weren't kept when the body had another lambda)
  @Test
  public void declaredTypesWithLambdaInBody() throws IOException {
    TokenList tokens =
        new Lexer()
            .getTokens("(Integer a, final String b) -> { Runnable r = () -> {}; return a; }");
    LambdaExpr lambda = (LambdaExpr) target.parse(tokens).tokens.get(0);
    assertEquals(list("Integer", "String"), lambda.paramTypes);
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Expression expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    Expression lambda = target.parse(tokens);
    assertEquals(expected, lambda);
    assertTokenIdx(tokens, (expected == null));
  }
}
