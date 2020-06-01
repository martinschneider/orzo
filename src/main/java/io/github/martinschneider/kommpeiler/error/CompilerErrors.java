package io.github.martinschneider.kommpeiler.error;

import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.ArrayList;
import java.util.List;

public class CompilerErrors {
  private List<CompilerError> errors = new ArrayList<>();

  public void setErrors(final List<CompilerError> errors) {
    this.errors = errors;
  }

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
                tokens.curr().getLoc(), loggerName, expected, tokens.curr())));
  }

  public int count() {
    return errors.size();
  }

  @Override
  public String toString() {
    StringBuffer stringBuffer = new StringBuffer();
    for (CompilerError error : errors) {
      stringBuffer.append("- ");
      stringBuffer.append(error.getMessage());
      stringBuffer.append("\n");
    }
    return stringBuffer.toString();
  }
}
