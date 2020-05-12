package io.github.martinschneider.kommpeiler.scanner;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.EQUAL;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.GREATER;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.GREATEREQ;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.NOTEQUAL;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.SMALLER;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.SMALLEREQ;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.DIV;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MINUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MOD;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.PLUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.TIMES;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.DOT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.cmp;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.github.martinschneider.kommpeiler.scanner.tokens.Comparator;
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
    assertEquals(((Integer) results.get(2).getValue()).intValue(), 12);
    assertEquals(((Integer) results.get(4).getValue()).intValue(), 13);
    assertEquals(((Integer) results.get(6).getValue()).intValue(), 15);
    assertEquals(((Integer) results.get(8).getValue()).intValue(), 17);
    results = scanner.getTokens("x=0.9");
    assertEquals(results.get(2).getClass(), DoubleNum.class);
    assertEquals(((Double) results.get(2).getValue()).doubleValue(), 0.9, 0);
    results = scanner.getTokens("y==.87");
    assertEquals(results.get(2).getClass(), DoubleNum.class);
    assertEquals(((Double) results.get(2).getValue()).doubleValue(), 0.87, 0);
  }

  @Test
  public void opTest() throws IOException {
    results = scanner.getTokens("1+2-3*4/5%6=7");
    for (int i = 1; i < results.size(); i += 2) {
      assertTrue(results.get(i) instanceof Operator);
    }
    assertEquals(results.get(1), op(PLUS));
    assertEquals(results.get(3), op(MINUS));
    assertEquals(results.get(5), op(TIMES));
    assertEquals(results.get(7), op(DIV));
    assertEquals(results.get(9), op(MOD));
    assertEquals(results.get(11), op(ASSIGN));
  }

  @Test
  public void cmpTest() throws IOException {
    results = scanner.getTokens("7<8<=9>10>=11!=12==13");
    for (int i = 1; i < results.size(); i += 2) {
      assertTrue(results.get(i) instanceof Comparator);
    }
    assertEquals(results.get(1), cmp(SMALLER));
    assertEquals(results.get(3), cmp(SMALLEREQ));
    assertEquals(results.get(5), cmp(GREATER));
    assertEquals(results.get(7), cmp(GREATEREQ));
    assertEquals(results.get(9), cmp(NOTEQUAL));
    assertEquals(results.get(11), cmp(EQUAL));
  }

  @Test
  public void symTest() throws IOException {
    results = scanner.getTokens("noch.ein,test;");
    for (int i = 1; i < results.size(); i += 2) {
      assertTrue(results.get(i) instanceof Sym);
    }
    assertEquals(results.get(1), sym(DOT));
    assertEquals(results.get(3), sym(COMMA));
    assertEquals(results.get(5), sym(SEMICOLON));
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
