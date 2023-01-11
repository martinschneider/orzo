package io.github.martinschneider.orzo.lexer;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.lexer.tokens.Keywords.*;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.*;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.*;
import static io.github.martinschneider.orzo.lexer.tokens.Token.*;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.*;
import static io.github.martinschneider.orzo.util.FactoryHelper.*;
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
        args("_ _test", new TokenList(list(id("_"), id("_test")))),
        args(
            "12  13 15 17",
            new TokenList(list(int32("12"), int32("13"), int32("15"), int32("17")))),
        args("0.9", new TokenList(list(float64("0.9")))),
        args(".87f", new TokenList(list(float32(".87")))),
        args(
            "1+2-3*4/5%6=7",
            new TokenList(
                list(
                    int32("1"),
                    op(PLUS),
                    int32("2"),
                    op(MINUS),
                    int32("3"),
                    op(TIMES),
                    int32("4"),
                    op(DIV),
                    int32("5"),
                    op(MOD),
                    int32("6"),
                    op(ASSIGN),
                    int32("7")))),
        args(
            "7<8<=9>10>=11!=12==13",
            new TokenList(
                list(
                    int32("7"),
                    op(LESS),
                    int32("8"),
                    op(LESSEQ),
                    int32("9"),
                    op(GREATER),
                    int32("10"),
                    op(GREATEREQ),
                    int32("11"),
                    op(NOTEQUAL),
                    int32("12"),
                    op(EQUAL),
                    int32("13")))),
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
        args("!x", new TokenList(list(op(NEGATE), id("x")))),
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
