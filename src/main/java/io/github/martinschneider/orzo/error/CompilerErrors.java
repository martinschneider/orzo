package io.github.martinschneider.orzo.error;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Location;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.ArrayList;
import java.util.List;

public class CompilerErrors {
  public List<CompilerError> errors = new ArrayList<>();
  public int tokenIdx;

  public void addError(String loggerName, String message) {
    errors.add(new CompilerError(String.format("%s: %s", loggerName, message)));
  }

  public void missingExpected(String loggerName, Token expected, TokenList tokens) {
    Location loc = tokens.get(tokenIdx).loc;
    errors.add(
        new CompilerError(
            String.format(
                "%s %s: expected %s but found %s",
                (loc != null) ? loc : "EOF", loggerName, expected, tokens.get(tokenIdx))));
  }

  public int count() {
    return errors.size();
  }

  @Override
  public String toString() {
    StringBuffer stringBuffer = new StringBuffer();
    for (CompilerError error : errors) {
      stringBuffer.append("- ");
      stringBuffer.append(error.msg);
      stringBuffer.append("\n");
    }
    return stringBuffer.toString();
  }
}
