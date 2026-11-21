package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class TryStatement implements Statement {
  public List<Statement> tryBody;
  public List<CatchBlock> catchBlocks;
  // statements of the finally block (null if there is none)
  public List<Statement> finallyBody;

  public TryStatement(List<Statement> tryBody, List<CatchBlock> catchBlocks) {
    this.tryBody = tryBody;
    this.catchBlocks = catchBlocks;
  }
}
