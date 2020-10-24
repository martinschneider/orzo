package io.github.martinschneider.orzo.lexer;

import static io.github.martinschneider.orzo.lexer.tokens.Comparators.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.NOTEQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.SMALLER;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.SMALLEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.DO;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.ELSE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.FOR;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.IF;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.PACKAGE;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.RETURN;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.STATIC;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.WHILE;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.cmp;
import static io.github.martinschneider.orzo.lexer.tokens.Token.fp;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.str;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.lexer.tokens.Token.type;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LexerTest {
  private Lexer scanner = new Lexer();

  private static Stream<Arguments> tokenTest() throws IOException {
    return stream(
        args("das ist ein test", new TokenList(list(id("das"), id("ist"), id("ein"), id("test")))),
        args("f1 f_1", new TokenList(list(id("f1"), id("f_1")))),
        args(
            "12  13 15 17",
            new TokenList(list(integer(12), integer(13), integer(15), integer(17)))),
        args("0.9", new TokenList(list(fp(0.9)))),
        args(".87", new TokenList(list(fp(0.87)))),
        args(
            "1+2-3*4/5%6=7",
            new TokenList(
                list(
                    integer(1),
                    op(PLUS),
                    integer(2),
                    op(MINUS),
                    integer(3),
                    op(TIMES),
                    integer(4),
                    op(DIV),
                    integer(5),
                    op(MOD),
                    integer(6),
                    op(ASSIGN),
                    integer(7)))),
        args(
            "7<8<=9>10>=11!=12==13",
            new TokenList(
                list(
                    integer(7),
                    cmp(SMALLER),
                    integer(8),
                    cmp(SMALLEREQ),
                    integer(9),
                    cmp(GREATER),
                    integer(10),
                    cmp(GREATEREQ),
                    integer(11),
                    cmp(NOTEQUAL),
                    integer(12),
                    cmp(EQUAL),
                    integer(13)))),
        args(".x", new TokenList(list(sym(DOT), id("x")))),
        args(
            "noch.ein,test;",
            new TokenList(
                list(id("noch"), sym(DOT), id("ein"), sym(COMMA), id("test"), sym(SEMICOLON)))),
        args(
            "String x = \"Halleluja!\"",
            new TokenList(list(type("String"), id("x"), op(ASSIGN), str("Halleluja!")))),
        args(
            "void int double String",
            new TokenList(list(type("void"), type("int"), type("double"), type("String")))),
        args(
            "if else do while for return static class package",
            new TokenList(
                list(
                    keyword(IF),
                    keyword(ELSE),
                    keyword(DO),
                    keyword(WHILE),
                    keyword(FOR),
                    keyword(RETURN),
                    keyword(STATIC),
                    keyword(CLASS),
                    keyword(PACKAGE)))),
        args(
            "public private protected",
            new TokenList(list(scope(PUBLIC), scope(PRIVATE), scope(PROTECTED)))),
        args("/* das ist ein kommentar */ /*und noch einer*/", new TokenList(emptyList())),
        args("// test \\n", new TokenList(emptyList())),
        args("/* abc//123\"\"\\$@ */ /*und noch einer*/\n//x=y+1;\n", new TokenList(emptyList())),
        args("// test", new TokenList(emptyList())),
        args("/* /* /* test */ */ */", new TokenList(emptyList())),
        args(
            "System.out.println",
            new TokenList(list(id("System"), sym(DOT), id("out"), sym(DOT), id("println")))));
  }

  @MethodSource
  @ParameterizedTest
  public void tokenTest(String input, TokenList expectedIdentifier) throws IOException {
    assertEquals(expectedIdentifier, scanner.getTokens(input));
  }

  @Test
  public void randomInputTest() throws IOException {
    final int nrOfChars = 1000000;
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 1; i <= nrOfChars; i++) {
      stringBuffer.append((char) (int) (Math.random() * 256 + 1));
    }
    scanner.getTokens(stringBuffer.toString());
  }
}
