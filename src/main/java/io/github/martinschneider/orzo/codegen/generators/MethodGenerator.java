package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.List;

public class MethodGenerator {

  public CGContext ctx;

  public MethodGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public HasOutput generate(HasOutput out, Method method, Clazz clazz) {
    out.write(method.accessFlags(clazz.isInterface));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, method.name.val));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, TypeUtils.methodDescr(method)));
    DynamicByteArray methodOut = new DynamicByteArray();
    if (!clazz.isInterface) {
      out.write((short) 1); // attribute size
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Code"));
      boolean returned = false;
      if (method instanceof Constructor && !startsWithCallToSuper(method.body)) {
        ctx.methodCallGen.callSuperConstr(methodOut);
      }
      for (Statement stmt : method.body) {
        generateCode(methodOut, method, stmt);
        if (stmt instanceof ReturnStatement) {
          returned = true;
        }
      }
      if (!returned) {
        methodOut.write(RETURN);
      }
      out.write(methodOut.size() + 12); // stack size (2) + local var size (2) + code size (4) +
      // exception table size (2) + attribute count size (2)
      out.write((short) (ctx.opStack.maxSize() + 1)); // max stack size
      out.write((short) (ctx.classIdMap.variables.localSize + 1)); // max local var size
      out.write(methodOut.size());
      out.write(methodOut.flush());
      out.write((short) 0); // exception table of size 0
      out.write((short) 0); // attribute count for this attribute of 0
    } else {
      out.write((short) 0); // attribute size
      out.write(methodOut.flush());
    }
    ctx.opStack.reset();
    return out;
  }

  public boolean startsWithCallToSuper(List<Statement> body) {
    if (body.isEmpty() || !(body.get(0) instanceof MethodCall)) {
      return false;
    }
    MethodCall call = (MethodCall) body.get(0);
    return "super".equals(call.name);
  }

  private void generateCode(DynamicByteArray out, Method method, Statement stmt) {
    ctx.delegator.generate(out, method, stmt);
  }
}
