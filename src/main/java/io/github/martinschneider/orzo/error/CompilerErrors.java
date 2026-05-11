package io.github.martinschneider.orzo.error;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.ArrayList;
import java.util.List;

public class CompilerErrors {

  public List<CompilerError> errors = new ArrayList<>();
  public int tokenIdx;

  public void addError(String loggerName, String message, StackTraceElement[] trace) {
    String msg = loggerName + ": " + message;
    errors.add(new CompilerError(msg, trace));
  }

  public void missingExpected(
      String loggerName, Token expected, TokenList tokens, StackTraceElement[] trace) {
    String msg =
        loggerName
            + ": expected "
            + expected.toString()
            + " but found "
            + tokens.get(tokenIdx).toString();
    errors.add(new CompilerError(msg, trace));
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
