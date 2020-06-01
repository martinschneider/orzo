package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.assign;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.cond;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.decl;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.doStmt;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.exp;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.ifBlk;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.ifStmt;
import static io.github.martinschneider.kommpeiler.parser.TestHelper.whileStmt;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.lexer.Lexer;
import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StatementParserTest {
  private StatementParser target = new StatementParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> test() throws IOException {
    return Stream.of(
        Arguments.of("x=5*12-3/6+12;", assign("x=5*12-3/6+12")),
        Arguments.of(
            "if (x==1){ x=2; }",
            ifStmt(List.of(ifBlk(cond("x==1"), List.of(assign("x=2")))), false)),
        Arguments.of("while (x>=0){ x=x-1; }", whileStmt(cond("x>=0"), List.of(assign("x=x-1")))),
        Arguments.of(
            "do{x=y+1;y=y-1;} while(i>0)",
            doStmt(cond("i>0"), List.of(assign("x=y+1"), assign("y=y-1")))),
        Arguments.of("int z;", decl(id("z"), "int", null, false)),
        Arguments.of("int z=300;", decl(id("z"), "int", exp("300"), true)));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, Statement expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }

  private static Stream<Arguments> testStmtSeq() throws IOException {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("x=5*12-3/6+12%4;", List.of(assign("x=5*12-3/6+12%4"))),
        Arguments.of("x=5*12-3/6+12%4;abc=9", List.of(assign("x=5*12-3/6+12%4"), assign("abc=9"))),
        Arguments.of("y=9%(4+7)*3;", List.of(assign("y=9%(4+7)*3"))),
        Arguments.of(
            "int x=10;x=x+1;int y=20;y=x+2;double z=x+y",
            List.of(
                decl("int x=10"),
                assign("x=x+1"),
                decl("int y=20"),
                assign("y=x+2"),
                decl("double z=x+y"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testStmtSeq(String input, List<Statement> expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parseStmtSeq(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
