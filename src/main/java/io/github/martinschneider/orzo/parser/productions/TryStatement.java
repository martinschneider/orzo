package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class TryStatement implements Statement {
  public List<Statement> tryBody;
  public List<CatchBlock> catchBlocks;

  public TryStatement(List<Statement> tryBody, List<CatchBlock> catchBlocks) {
    this.tryBody = tryBody;
    this.catchBlocks = catchBlocks;
  }
}
