package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.parser.TestHelper.arg;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.assign;
import static io.github.martinschneider.orzo.parser.TestHelper.doStmt;
import static io.github.martinschneider.orzo.parser.TestHelper.expr;
import static io.github.martinschneider.orzo.parser.TestHelper.ifStmt;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.method;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static io.github.martinschneider.orzo.parser.TestHelper.whileStmt;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MethodParserTest {
  private MethodParser target = new MethodParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("{}", null),
        args("method()", null),
        args(
            "public void test(){x=1;y=2;if(x==y){fehler=1;}}",
            method(
                scope(PUBLIC),
                "void",
                id("test"),
                emptyList(),
                list(
                    assign("x=1"),
                    assign("y=2"),
                    ifStmt(list(new IfBlock(expr("x==y"), list(assign("fehler=1")))), false)))),
        args(
            "void test(){x=100;while(x>0){x=x-1;}}",
            method(
                scope(DEFAULT),
                "void",
                id("test"),
                emptyList(),
                list(assign("x=100"), whileStmt(expr("x>0"), list(assign("x=x-1")))))),
        args(
            "protected int huber(){do{}while(x>0)}",
            method(
                scope(PROTECTED),
                "int",
                id("huber"),
                emptyList(),
                list(doStmt(expr("x>0"), emptyList())))),
        args(
            "private double calculateMean(){}",
            method(scope(PRIVATE), "double", id("calculateMean"), emptyList(), emptyList())),
        args(
            "public byte[] test(){}",
            method(scope(PUBLIC), "[byte", id("test"), emptyList(), emptyList())));
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, Method expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }

  private static Stream<Arguments> testArgs() throws IOException {
    return stream(
        args("", null, emptyList()),
        args("(", null),
        args(")", null),
        args("a, b, c", null),
        args("int a", list(arg(INT, id("a")))),
        args(
            "int a, boolean b, double d",
            list(arg(INT, id("a")), arg(BOOLEAN, id("b")), arg(DOUBLE, id("d")))));
  }

  @ParameterizedTest
  @MethodSource
  public void testArgs(String input, List<Argument> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parseArgs(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
