package io.github.martinschneider.kommpeiler.error;

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

  public List<CompilerError> getScannerErrors() {
    List<CompilerError> returnList = new ArrayList<>();
    for (CompilerError error : errors) {
      if (error.getType().equals(ErrorType.SCANNER)) {
        returnList.add(error);
      }
    }
    return returnList;
  }

  public List<CompilerError> getParserErrors() {
    List<CompilerError> returnList = new ArrayList<>();
    for (CompilerError error : errors) {
      if (error.getType().equals(ErrorType.PARSER)) {
        returnList.add(error);
      }
    }
    return returnList;
  }

  public void addError(final CompilerError error) {
    errors.add(error);
  }

  public void addError(final String message, final ErrorType type) {
    errors.add(new CompilerError(message, type));
  }

  public void addScannerError(final String message) {
    errors.add(new CompilerError(message, ErrorType.SCANNER));
  }

  public void addParserError(final String message) {
    errors.add(new CompilerError(message, ErrorType.PARSER));
  }

  public void addCodegenError(final String message) {
    errors.add(new CompilerError(message, ErrorType.CODEGEN));
  }

  public int count() {
    return errors.size();
  }

  public void clear() {
    errors.clear();
  }

  @Override
  public String toString() {
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 0; i < errors.size(); i++) {
      stringBuffer.append(i + 1);
      stringBuffer.append(". ");
      stringBuffer.append(errors.get(i).getMessage());
      stringBuffer.append("\n");
    }
    return stringBuffer.toString();
  }
}
