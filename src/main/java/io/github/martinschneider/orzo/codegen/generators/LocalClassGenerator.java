package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.InnerClasses;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.LocalClassDeclaration;
import io.github.martinschneider.orzo.parser.productions.Method;

// No code: a local class (generated later) becomes inner (unless in a static context) and captures
// the local variables it uses (passed to its constructors as their last arguments).
public class LocalClassGenerator implements StatementGenerator<LocalClassDeclaration> {
  private CGContext ctx;

  public LocalClassGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, LocalClassDeclaration decl) {
    Clazz local = decl.clazz;
    ctx.localClasses.put(local.name, local);
    // (local interfaces and enums are static; a statement may be generated more than once, e.g.
    // in a finally block)
    if (local.isInterface || local.isEnum || local.captured != null) {
      return out;
    }
    local.captured =
        ExpressionGenerator.asArguments(ctx.exprGen.capturedBy(local.methods, local.instanceInits));
    if (!ctx.exprGen.isStaticContext()) {
      InnerClasses.makeInner(local, ctx.clazz);
    }
    InnerClasses.addFields(local, false, local.captured);
    return out;
  }
}
