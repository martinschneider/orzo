package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class CatchBlock {
  // more than one for a multi-catch, e.g. "catch (IOException | RuntimeException e)"
  public List<String> exceptionTypes;
  public String varName;
  public List<Statement> body;

  public CatchBlock(List<String> exceptionTypes, String varName, List<Statement> body) {
    this.exceptionTypes = exceptionTypes;
    this.varName = varName;
    this.body = body;
  }
}
