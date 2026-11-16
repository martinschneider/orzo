package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.POP;
import static io.github.martinschneider.orzo.codegen.OpCodes.POP2;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;

public class ConstructorCallGenerator implements StatementGenerator<ConstructorCall> {

  private CGContext ctx;

  public ConstructorCallGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ConstructorCall constructorCall) {
    // Use the existing ExpressionGenerator logic for constructor calls
    String retType = ctx.exprGen.generateConstructorCall(out, ctx.classIdMap, constructorCall);
    // Chained calls as a standalone statement, e.g. "new Foo().bar().baz();" — each subsequent
    // call is invoked on the previous call's return value, left on the stack.
    Identifier chainNext = constructorCall.next;
    while (chainNext instanceof MethodCall) {
      retType =
          ctx.methodCallGen.generateChained(out, ctx.classIdMap, (MethodCall) chainNext, retType);
      chainNext = chainNext.next;
    }
    if (retType != null && !retType.isEmpty() && !VOID.equals(retType)) {
      if (LONG.equals(retType) || DOUBLE.equals(retType)) {
        out.write(POP2);
      } else {
        out.write(POP);
      }
      ctx.opStack.pop();
    }
    return out;
  }
}
