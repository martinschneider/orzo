package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.util.FactoryHelper.*;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StatementParserTest {
  private StatementParser target = new StatementParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("x=5*12-3/6+12;", assign("x=5*12-3/6+12")),
        args("if (x==1){ x=2; }", ifStmt(list(ifBlk(expr("x==1"), list(assign("x=2")))), false)),
        args("while (x>=0){ x=x-1; }", whileStmt(expr("x>=0"), list(assign("x=x-1")))),
        args(
            "do{x=y+1;y=y-1;} while(i>0)",
            doStmt(expr("i>0"), list(assign("x=y+1"), assign("y=y-1")))),
        args("int z;", pDecl(emptyList(), id("z"), "int", null)),
        args("int z=300;", pDecl(emptyList(), id("z"), "int", expr("300"))),
        args("this.a=a;", assign("this.a=a")));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Statement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }

  private static Stream<Arguments> testStmtSeq() throws IOException {
    return stream(
        args("", null),
        args("x=5*12-3/6+12%4;", list(assign("x=5*12-3/6+12%4"))),
        args("x=5*12-3/6+12%4;abc=9", list(assign("x=5*12-3/6+12%4"), assign("abc=9"))),
        args("y=9%(4+7)*3;", list(assign("y=9%(4+7)*3"))),
        args(
            "int x=10;x=x+1;int y=20;y=x+2;double z=x+y",
            list(
                pDecl("int x=10"),
                assign("x=x+1"),
                pDecl("int y=20"),
                assign("y=x+2"),
                pDecl("double z=x+y"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testStmtSeq(String input, List<Statement> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parseStmtSeq(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
