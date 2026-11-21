package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Continue;
import io.github.martinschneider.orzo.parser.productions.Method;

/**
 * Emits a placeholder GOTO for a "continue" statement and records its offset field in {@code
 * out.pendingContinues} so the enclosing loop generator can patch it once the jump target (the
 * loop's condition check or increment step) is known. Works no matter how deeply the continue is
 * nested (e.g. inside an if-block), since nested generators propagate pending positions up via
 * {@code mergePending}.
 */
public class ContinueGenerator implements StatementGenerator<Continue> {
  private CGContext ctx;

  public ContinueGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, Continue stmt) {
    if (stmt.label != null) {
      out.labeledContinues
          .computeIfAbsent(stmt.label, k -> new java.util.ArrayList<>())
          .add(out.size());
    } else {
      out.pendingContinues.add(out.size());
    }
    out.write(GOTO);
    out.write((short) 0); // placeholder, patched by the enclosing loop generator
    return out;
  }
}
