package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;

/**
 * Emits a placeholder GOTO for a "break" statement and records its offset field in {@code
 * out.pendingBreaks} so the enclosing loop or switch generator can patch it once the jump target
 * (loop/switch exit) is known. Works no matter how deeply the break is nested (e.g. inside an
 * if-block), since nested generators propagate pending positions up via {@code mergePending}.
 */
public class BreakGenerator implements StatementGenerator<Break> {
  private CGContext ctx;

  public BreakGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, Break stmt) {
    Statement jumpTarget = null;
    for (Statement target : ctx.jumpTargets) {
      if (jumpTarget == null
          && ((stmt.label != null)
              ? stmt.label.equals(CGContext.label(target))
              : CGContext.isLoop(target) || target instanceof SwitchStatement)) {
        jumpTarget = target;
      }
    }
    if (jumpTarget == null) {
      ctx.errors.addError(
          "break code generator",
          (stmt.label != null)
              ? "undefined label: " + stmt.label
              : "break outside of a loop or switch",
          new RuntimeException().getStackTrace());
      return out;
    }
    TryGenerator.runFinallyBlocks(ctx, out, method, jumpTarget);
    if (stmt.label != null) {
      out.labeledBreaks
          .computeIfAbsent(stmt.label, k -> new java.util.ArrayList<>())
          .add(out.size());
    } else {
      out.pendingBreaks.add(out.size());
    }
    out.write(GOTO);
    out.write((short) 0); // placeholder, patched by the enclosing loop/switch generator
    return out;
  }
}
