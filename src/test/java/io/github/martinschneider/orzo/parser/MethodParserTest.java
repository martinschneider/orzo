package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Type.*;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.*;
import static io.github.martinschneider.orzo.parser.productions.Method.CONSTRUCTOR_NAME;
import static io.github.martinschneider.orzo.util.FactoryHelper.*;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Clazz;
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
  private static final Clazz clazz =
      clazz(
          "",
          emptyList(),
          scope(PUBLIC),
          "Martin",
          emptyList(),
          Clazz.JAVA_LANG_OBJECT,
          emptyList(),
          emptyList());

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("", null),
        args("{}", null),
        args("method()", null),
        args(
            "public void test(){x=1;y=2;if(x==y){fehler=1;}}",
            method(
                list(ACC_PUBLIC),
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
                emptyList(),
                "void",
                id("test"),
                emptyList(),
                list(assign("x=100"), whileStmt(expr("x>0"), list(assign("x=x-1")))))),
        args(
            "protected int huber(){do{}while(x>0)}",
            method(
                list(ACC_PROTECTED),
                "int",
                id("huber"),
                emptyList(),
                list(doStmt(expr("x>0"), emptyList())))),
        args(
            "private double calculateMean(){}",
            method(list(ACC_PRIVATE), "double", id("calculateMean"), emptyList(), emptyList())),
        args(
            "public byte[] test(){}",
            method(list(ACC_PUBLIC), "[byte", id("test"), emptyList(), emptyList())),
        args(
            "public static byte[] test(){}",
            method(list(ACC_PUBLIC, ACC_STATIC), "[byte", id("test"), emptyList(), emptyList())),
        args(
            "public Martin(){x=1;y=2;}",
            constr(
                scope(PUBLIC),
                VOID,
                id(CONSTRUCTOR_NAME),
                emptyList(),
                list(assign("x=1"), assign("y=2")))));
  }

  @MethodSource
  @ParameterizedTest
  public void test(String input, Method expected) throws IOException {
    target.ctx.currClazz = clazz;
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
