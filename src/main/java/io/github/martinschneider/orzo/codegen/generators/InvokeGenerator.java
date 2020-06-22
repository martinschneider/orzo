package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKESTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEW;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.parser.productions.Method;

public class InvokeGenerator {
  public CGContext ctx;

  public InvokeGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public HasOutput getStatic(DynamicByteArray out, String clazz, String field, String type) {
    out.write(GETSTATIC);
    out.write(ctx.constPool.indexOf(CONSTANT_FIELDREF, clazz, field, type));
    ctx.opStack.push(REF);
    return out;
  }

  public HasOutput invokeVirtual(DynamicByteArray out, Method method) {
    out.write(INVOKEVIRTUAL);
    out.write(
        ctx.constPool.indexOf(
            CONSTANT_METHODREF,
            method.fqClassName,
            method.name.id(),
            TypeUtils.methodDescr(method)));
    ctx.opStack.pop(method.args.size());
    ctx.opStack.push(method.type);
    return out;
  }

  public HasOutput invokeStatic(DynamicByteArray out, Method method) {
    String methodName = method.name.id();
    String clazzName = ctx.clazz.fqn('/');
    if (methodName.contains(".")) {
      String[] tmp = methodName.split("\\.");
      clazzName = method.fqClassName.replaceAll("\\.", "/");
      methodName = tmp[1];
      ctx.constPool.addClass(clazzName);
      ctx.constPool.addMethodRef(clazzName, methodName, TypeUtils.methodDescr(method));
    } else if (!ctx.clazz.fqn().equals(method.fqClassName)) { // static import
      clazzName = method.fqClassName.replaceAll("\\.", "/");
      ctx.constPool.addClass(clazzName);
      ctx.constPool.addMethodRef(clazzName, methodName, TypeUtils.methodDescr(method));
    }
    out.write(INVOKESTATIC);
    out.write(
        ctx.constPool.indexOf(
            CONSTANT_METHODREF, clazzName, methodName, TypeUtils.methodDescr(method)));
    ctx.opStack.pop(method.args.size());
    ctx.opStack.push(method.type);
    return out;
  }

  public HasOutput newInstance(DynamicByteArray out, String clazz) {
    out.write(NEW);
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, clazz));
    return out;
  }
}
