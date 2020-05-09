package io.github.martinschneider.kommpeiler.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.scanner.tokens.DoubleNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Keyword;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Sym;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unittests for the scanner.
 *
 * @author Martin Schneider
 */
public class ScannerTest {

  private Scanner scanner = new Scanner();
  private List<Token> results;

  @Test
  public void identifierTest() throws IOException {
    results = scanner.getTokens("das ist ein test");
    for (Token token : results) {
      assertEquals(token.getClass(), Identifier.class);
    }
    assertEquals(((Identifier) results.get(0)).getValue(), "das");
    assertEquals(((Identifier) results.get(1)).getValue(), "ist");
    assertEquals(((Identifier) results.get(2)).getValue(), "ein");
    assertEquals(((Identifier) results.get(3)).getValue(), "test");
  }

  @Test
  public void numTest() throws IOException {
    results = scanner.getTokens("x=12+13+15+17");
    for (int i = 2; i <= 8; i += 2) {
      assertEquals(results.get(i).getClass(), IntNum.class);
    }

    assertEquals(((IntNum) results.get(2)).parseValue(), 12);
    assertEquals(((IntNum) results.get(4)).parseValue(), 13);
    assertEquals(((IntNum) results.get(6)).parseValue(), 15);
    assertEquals(((IntNum) results.get(8)).parseValue(), 17);

    results = scanner.getTokens("x=0.9");
    assertEquals(results.get(2).getClass(), DoubleNum.class);
    assertEquals(((DoubleNum) results.get(2)).parseValue(), 0.9, 0);

    results = scanner.getTokens("y==.87");
    assertEquals(results.get(2).getClass(), DoubleNum.class);
    assertEquals(((DoubleNum) results.get(2)).parseValue(), 0.87, 0);
  }

  @Test
  public void opTest() throws IOException {
    results = scanner.getTokens("1+2-3*4/5%6=7<8<=9>10>=11!=12==13");
    for (int i = 1; i <= 23; i += 2) {
      assertEquals(results.get(i).getClass(), Operator.class);
    }
    assertEquals(((Operator) results.get(1)).getValue(), "PLUS");
    assertEquals(((Operator) results.get(3)).getValue(), "MINUS");
    assertEquals(((Operator) results.get(5)).getValue(), "TIMES");
    assertEquals(((Operator) results.get(7)).getValue(), "DIV");
    assertEquals(((Operator) results.get(9)).getValue(), "MOD");
    assertEquals(((Operator) results.get(11)).getValue(), "ASSIGN");
    assertEquals(((Operator) results.get(13)).getValue(), "SMALLER");
    assertEquals(((Operator) results.get(15)).getValue(), "SMALLEREQ");
    assertEquals(((Operator) results.get(17)).getValue(), "GREATER");
    assertEquals(((Operator) results.get(19)).getValue(), "GREATEREQ");
    assertEquals(((Operator) results.get(21)).getValue(), "NOTEQUAL");
    assertEquals(((Operator) results.get(23)).getValue(), "EQUAL");
  }

  @Test
  public void symTest() throws IOException {
    results = scanner.getTokens("noch.ein,test;");
    for (int i = 1; i <= 5; i += 2) {
      assertEquals(results.get(i).getClass(), Sym.class);
    }
    assertEquals(((Sym) results.get(1)).getValue(), "DOT");
    assertEquals(((Sym) results.get(3)).getValue(), "COMMA");
    assertEquals(((Sym) results.get(5)).getValue(), "SEMICOLON");
  }

  @Test
  public void strTest() throws IOException {
    results = scanner.getTokens("String x = \"Halleluja!\"");
    assertEquals(results.get(3).getClass(), Str.class);
    assertEquals(((Str) results.get(3)).getValue(), "Halleluja!");
  }

  @Test
  public void keywordTest() throws IOException {
    results = scanner.getTokens("if while");
    for (Token token : results) {
      assertEquals(token.getClass(), Keyword.class);
    }
  }

  /** @throws IOException I/O-error */
  @Test
  public void selectorTest() throws IOException {
    results = scanner.getTokens(".x");
    assertEquals(results.size(), 2);
  }

  @Test
  public void commentTest() throws IOException {
    results = scanner.getTokens("/* das ist ein kommentar */ /*und noch einer*/");
    assertEquals(results.size(), 0);

    results = scanner.getTokens("// test \n");
    assertEquals(results.size(), 0);

    results = scanner.getTokens("/* abc//123\"\"\\$@ */ /*und noch einer*/\n//x=y+1;\n");
    assertEquals(results.size(), 0);

    results = scanner.getTokens("// test");
    assertEquals(results.size(), 0);

    results = scanner.getTokens("/* /* /* test */ */ */");
    assertEquals(results.size(), 0);
  }

  public void errorTestComment() throws IOException {
    scanner.getTokens("/* test /* test2 */");
    assertEquals(scanner.getErrors().count(), 1);
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
