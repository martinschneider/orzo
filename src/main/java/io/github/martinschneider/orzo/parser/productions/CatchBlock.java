package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class CatchBlock {
  public String exceptionType;
  public String varName;
  public List<Statement> body;

  public CatchBlock(String exceptionType, String varName, List<Statement> body) {
    this.exceptionType = exceptionType;
    this.varName = varName;
    this.body = body;
  }
}
