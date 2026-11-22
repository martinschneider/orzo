package io.github.martinschneider.orzo.error;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The compile errors of a compilation run (shared by the lexer, the parsers and the code generator
 * of all its files). An error is reported at the current position: the current token while parsing,
 * the start of the statement being generated during code generation.
 */
public class CompilerErrors {
  public List<CompilerError> errors = new ArrayList<>();
  // the file being compiled
  public String file;
  // the tokens being parsed (null outside of parsing)
  public TokenList tokens;
  // the position used for errors without a token of their own (e.g. during code generation)
  public Token position;
  // the first token of each parsed statement and class member (by identity), for the positions of
  // errors during code generation
  public Map<Object, Token> startTokens = new IdentityHashMap<>();

  public void addError(String source, String message, StackTraceElement[] trace) {
    Token at = position;
    if (tokens != null && tokens.idx() < tokens.list().size()) {
      at = tokens.curr();
    }
    addError(at, source, message, trace);
  }

  public void addError(Token at, String source, String message, StackTraceElement[] trace) {
    int line = (at != null) ? at.line : 0;
    int col = (at != null) ? at.col : 0;
    CompilerError error = new CompilerError(file, line, col, source, message, trace);
    if (!errors.contains(error)) {
      errors.add(error);
    }
  }

  /** "expected ')' but found '{'" at the current token */
  public void missingExpected(
      String source, Token expected, TokenList tokens, StackTraceElement[] trace) {
    Token found = tokens.curr();
    addError(
        found, source, "expected " + expected.display() + " but found " + found.display(), trace);
  }

  public boolean isEmpty() {
    return errors.isEmpty();
  }

  @Override
  public String toString() {
    return toString(0);
  }

  // (verbose > 0: also the reporting component and that many frames of the compiler's call trace)
  public String toString(int verbose) {
    StringBuilder errorMsg = new StringBuilder();
    for (CompilerError error : errors) {
      errorMsg.append(error);
      if (verbose > 0) {
        errorMsg.append(" [").append(error.source).append("]");
        errorMsg.append(" call trace: ");
        for (int j = 0; j < verbose && j < error.trace.length; j++) {
          errorMsg.append(error.trace[j]);
          if (j < verbose - 1 && j < error.trace.length - 1) {
            errorMsg.append(" <- ");
          }
        }
      }
      errorMsg.append("\n");
    }
    int errCount = errors.size();
    errorMsg.append(errCount).append(errCount == 1 ? " error" : " errors").append("\n");
    return errorMsg.toString();
  }
}
