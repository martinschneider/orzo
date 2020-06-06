package io.github.martinschneider.orzo.error;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.ArrayList;
import java.util.List;

public class CompilerErrors {
  private List<CompilerError> errors = new ArrayList<>();

  public List<CompilerError> getErrors() {
    return errors;
  }

  public void addError(String loggerName, String message) {
    errors.add(new CompilerError(String.format("%s: %s", loggerName, message)));
  }

  public void addError(String loggerName, String message, TokenList tokens) {
    errors.add(new CompilerError(message));
  }

  public void missingExpected(String loggerName, Token expected, TokenList tokens) {
    errors.add(
        new CompilerError(
            String.format(
                "%s %s: expected %s but found %s",
                tokens.curr().loc, loggerName, expected, tokens.curr())));
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
