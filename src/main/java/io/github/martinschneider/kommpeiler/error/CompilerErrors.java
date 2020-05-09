package io.github.martinschneider.kommpeiler.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for compiler errors
 *
 * @author Martin Schneider
 */
public class CompilerErrors {
  private List<CompilerError> errors = new ArrayList<CompilerError>();

  public void setErrors(final List<CompilerError> errors) {
    this.errors = errors;
  }

  public List<CompilerError> getErrors() {
    return errors;
  }

  /** @return list of scanner errors */
  public List<CompilerError> getScannerErrors() {
    List<CompilerError> returnList = new ArrayList<CompilerError>();
    for (CompilerError error : errors) {
      if (error.getType().equals(ErrorType.SCANNER)) {
        returnList.add(error);
      }
    }
    return returnList;
  }

  /** @return list of parser errors */
  public List<CompilerError> getParserErrors() {
    List<CompilerError> returnList = new ArrayList<CompilerError>();
    for (CompilerError error : errors) {
      if (error.getType().equals(ErrorType.PARSER)) {
        returnList.add(error);
      }
    }
    return returnList;
  }

  /** @param error error */
  public void addError(final CompilerError error) {
    errors.add(error);
  }

  /**
   * @param message error-message
   * @param type error-type
   */
  public void addError(final String message, final ErrorType type) {
    errors.add(new CompilerError(message, type));
  }

  /** @param message error-message */
  public void addScannerError(final String message) {
    errors.add(new CompilerError(message, ErrorType.SCANNER));
  }

  /** @param message error-message */
  public void addParserError(final String message) {
    errors.add(new CompilerError(message, ErrorType.PARSER));
  }

  /** @return error-count */
  public int count() {
    return errors.size();
  }

  /** clear errors */
  public void clear() {
    errors.clear();
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    StringBuffer stringBuffer = new StringBuffer("Errors: ");
    for (CompilerError error : errors) {
      stringBuffer.append(" ");
      stringBuffer.append(error.getMessage());
    }
    return stringBuffer.toString();
  }
}
