package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Method;

public class ConstructorCallGenerator implements StatementGenerator<ConstructorCall> {

  private CGContext ctx;

  public ConstructorCallGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ConstructorCall constructorCall) {
    // Use the existing ExpressionGenerator logic for constructor calls
    ctx.exprGen.generateConstructorCall(out, ctx.classIdMap, constructorCall);
    return out;
  }
}
