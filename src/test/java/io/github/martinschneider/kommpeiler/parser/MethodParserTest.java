package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.arg;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assign;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.cond;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.doStmt;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.ifStmt;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.method;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.whileStmt;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BOOLEAN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.IfBlock;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.TokenList;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MethodParserTest {
  private MethodParser target = new MethodParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("{}", null),
        Arguments.of("method()", null),
        Arguments.of(
            "public void test(){x=1;y=2;if(x==y){fehler=1;}}",
            method(
                scope(PUBLIC),
                "void",
                id("test"),
                emptyList(),
                List.of(
                    assign("x=1"),
                    assign("y=2"),
                    ifStmt(
                        List.of(new IfBlock(cond("x==y"), List.of(assign("fehler=1")))), false)))),
        Arguments.of(
            "void test(){x=100;while(x>0){x=x-1;}}",
            method(
                scope(DEFAULT),
                "void",
                id("test"),
                emptyList(),
                List.of(assign("x=100"), whileStmt(cond("x>0"), List.of(assign("x=x-1")))))),
        Arguments.of(
            "protected int huber(){do{}while(x>0)}",
            method(
                scope(PROTECTED),
                "int",
                id("huber"),
                emptyList(),
                List.of(doStmt(cond("x>0"), emptyList())))),
        Arguments.of(
            "private double calculateMean(){}",
            method(scope(PRIVATE), "double", id("calculateMean"), emptyList(), emptyList())));
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, Method expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }

  private static Stream<Arguments> testArgs() throws IOException {
    return Stream.of(
        Arguments.of("", null, emptyList()),
        Arguments.of("(", null),
        Arguments.of(")", null),
        Arguments.of("a, b, c", null),
        Arguments.of("int a", List.of(arg(INT, id("a")))),
        Arguments.of(
            "int a, boolean b, double d",
            List.of(arg(INT, id("a")), arg(BOOLEAN, id("b")), arg(DOUBLE, id("d")))));
  }

  @ParameterizedTest
  @MethodSource
  public void testArgs(String input, List<Argument> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parseArgs(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
