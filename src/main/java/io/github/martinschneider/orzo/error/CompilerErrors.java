package io.github.martinschneider.orzo.error;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Location;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.ArrayList;
import java.util.List;

public class CompilerErrors {

  public List<CompilerError> errors = new ArrayList<>();
  public int tokenIdx;

  public void addError(String loggerName, String message, StackTraceElement[] trace) {
    errors.add(new CompilerError(String.format("%s: %s", loggerName, message), trace));
  }

  public void missingExpected(
      String loggerName, Token expected, TokenList tokens, StackTraceElement[] trace) {
    Location loc = tokens.get(tokenIdx).loc;
    errors.add(
        new CompilerError(
            String.format(
                "%s %s: expected %s but found %s",
                (loc != null) ? loc : "EOF", loggerName, expected, tokens.get(tokenIdx)),
            trace));
  }

  @Override
  public String toString() {
    return toString(0);
  }

  public String toString(int verbose) {
    StringBuilder errorMsg = new StringBuilder();
    int errCount = errors.size();
    errorMsg.append(errCount);
    errorMsg.append(" error");
    if (errCount > 1) {
      errorMsg.append("s");
    }
    errorMsg.append("\n");
    for (CompilerError error : errors) {
      errorMsg.append(error);
      if (verbose > 0) {
        errorMsg.append(". call trace: ");
        for (int j = 0; j < verbose && j < error.trace.length; j++) {
          errorMsg.append(error.trace[j]);
          if (j < verbose - 1 && j < error.trace.length - 1) {
            errorMsg.append(" <- ");
          }
        }
      }
      errorMsg.append("\n");
    }
    return errorMsg.toString();
  }
}
