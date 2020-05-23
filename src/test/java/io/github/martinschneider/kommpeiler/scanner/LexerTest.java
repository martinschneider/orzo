package io.github.martinschneider.kommpeiler.scanner;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.EQUAL;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.GREATER;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.GREATEREQ;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.NOTEQUAL;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.SMALLER;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.SMALLEREQ;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.CLASS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.DO;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.ELSE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.FOR;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.IF;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.PACKAGE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.RETURN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.STATIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.WHILE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.DIV;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MINUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MOD;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.PLUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.TIMES;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.DOT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.cmp;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.fp;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.integer;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.str;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LexerTest {
  private Lexer scanner = new Lexer();

  private static Stream<Arguments> tokenTest() throws IOException {
    return Stream.of(
        Arguments.of("das ist ein test", List.of(id("das"), id("ist"), id("ein"), id("test"))),
        Arguments.of("f1 f_1", List.of(id("f1"), id("f_1"))),
        Arguments.of("12  13 15 17", List.of(integer(12), integer(13), integer(15), integer(17))),
        Arguments.of("0.9", List.of(fp(0.9))),
        Arguments.of(".87", List.of(fp(0.87))),
        Arguments.of(
            "1+2-3*4/5%6=7",
            List.of(
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
                integer(7))),
        Arguments.of(
            "7<8<=9>10>=11!=12==13",
            List.of(
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
                integer(13))),
        Arguments.of(".x", List.of(sym(DOT), id("x"))),
        Arguments.of(
            "noch.ein,test;",
            List.of(id("noch"), sym(DOT), id("ein"), sym(COMMA), id("test"), sym(SEMICOLON))),
        Arguments.of(
            "String x = \"Halleluja!\"",
            List.of(type("String"), id("x"), op(ASSIGN), str("Halleluja!"))),
        Arguments.of(
            "void int double String",
            List.of(type("void"), type("int"), type("double"), type("String"))),
        Arguments.of(
            "if else do while for return static class package",
            List.of(
                keyword(IF),
                keyword(ELSE),
                keyword(DO),
                keyword(WHILE),
                keyword(FOR),
                keyword(RETURN),
                keyword(STATIC),
                keyword(CLASS),
                keyword(PACKAGE))),
        Arguments.of(
            "public private protected", List.of(scope(PUBLIC), scope(PRIVATE), scope(PROTECTED))),
        Arguments.of("/* das ist ein kommentar */ /*und noch einer*/", Collections.emptyList()),
        Arguments.of("// test \\n", Collections.emptyList()),
        Arguments.of(
            "/* abc//123\"\"\\$@ */ /*und noch einer*/\n//x=y+1;\n", Collections.emptyList()),
        Arguments.of("// test", Collections.emptyList()),
        Arguments.of("/* /* /* test */ */ */", Collections.emptyList()));
  }

  @MethodSource
  @ParameterizedTest
  public void tokenTest(String input, List<Token> expectedIdentifier) throws IOException {
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
