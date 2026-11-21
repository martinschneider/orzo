package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.List;

public class BlockGenerator implements StatementGenerator<Block> {
  private CGContext ctx;

  public BlockGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, Block block) {
    if (block.label == null) {
      for (Statement stmt : block.body) {
        ctx.delegator.generate(out, method, stmt);
      }
      return out;
    }
    // A labeled block: "break label;" inside it jumps to its end.
    int etStart = ctx.exceptionTable.size();
    DynamicByteArray bodyOut = new DynamicByteArray();
    for (Statement stmt : block.body) {
      ctx.delegator.generate(bodyOut, method, stmt);
    }
    TryGenerator.adjustExceptionTableEntries(ctx, etStart, ctx.exceptionTable.size(), out.size());
    byte[] bodyBytes = bodyOut.getBytes();
    List<Integer> breaks = bodyOut.labeledBreaks.remove(block.label);
    if (breaks != null) {
      for (int pos : breaks) {
        byte[] off = shortToByteArray((short) (bodyBytes.length - pos));
        bodyBytes[pos + 1] = off[0];
        bodyBytes[pos + 2] = off[1];
      }
    }
    // other breaks/continues belong to enclosing statements
    out.mergePending(bodyOut);
    out.write(bodyBytes);
    return out;
  }
}
