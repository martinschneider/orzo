package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ParserTest {
  private Parser target = new Parser(new CompilerErrors());

  @Test
  public void randomInputTest() throws IOException {
    final int nrOfChars = 1000000;
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 1; i <= nrOfChars; i++) {
      stringBuffer.append((char) (int) (Math.random() * 256 + 1));
    }
    try {
      target.parse(new Lexer().getTokens(stringBuffer.toString()));
    } catch (NumberFormatException e) {
      // this is okay (random inputs can have "0x" followed not by a valid hex number)
    }
  }

  @Test
  public void randomTokensTest() throws IOException {
    final int nrOfTokens = 100000;
    List<String> tokenList =
        Arrays.asList(
            "while",
            "do",
            "if",
            "public",
            "private",
            "protected",
            "x",
            "y",
            "0",
            "=",
            "==",
            "!=",
            "{",
            "}",
            "<",
            ">",
            ">=",
            "<=",
            "+",
            "-",
            "[",
            "]");
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 1; i <= nrOfTokens; i++) {
      int index = (int) (Math.random() * tokenList.size());
      stringBuffer.append(tokenList.get(index));
      stringBuffer.append(" ");
    }
    target.parse(new Lexer().getTokens(stringBuffer.toString()));
  }
}
