package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.List;

public class CodeGenerator {
  // for higher versions the JVM enforces stricter bytecode verification
  // TODO: implement StackMapTable attribute:
  // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.4
  private static final short JAVA_CLASS_MAJOR_VERSION = 50;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  private CGContext ctx;
  private List<Output> outputs;
  private List<Clazz> clazzes;
  private CompilerErrors errors;
  Output out;

  public CodeGenerator(List<Clazz> clazzes, List<Output> outputs, CompilerErrors errors) {
    this.outputs = outputs;
    this.clazzes = clazzes;
    this.errors = errors;
    ctx = new CGContext();
  }

  public CompilerErrors getErrors() {
    return ctx.errors;
  }

  void accessModifiers(Clazz clazz) {
    short modifiers = (short) (AccessFlag.ACC_SUPER.val + AccessFlag.ACC_PUBLIC.val);
    if (clazz.isInterface) {
      modifiers =
          (short)
              (AccessFlag.ACC_INTERFACE.val
                  + AccessFlag.ACC_ABSTRACT.val
                  + AccessFlag.ACC_PUBLIC.val);
    } else if (clazz.isEnum) {
      modifiers =
          (short)
              (AccessFlag.ACC_PUBLIC.val
                  + AccessFlag.ACC_FINAL.val
                  + AccessFlag.ACC_SUPER.val
                  + AccessFlag.ACC_ENUM.val);
    }
    out.write(modifiers);
  }

  private void attributes() {
    out.write((short) 1);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "SourceFile"));
    out.write(2);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, ctx.clazz.sourceFile));
  }

  private void classIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, ctx.clazz.fqn('/')));
  }

  private HasOutput constPool(HasOutput out) {
    out.write(ctx.constPool.getBytes());
    return out;
  }

  private void fields() {
    out.write((short) ctx.classIdMap.variables.fieldSize);
    for (VariableInfo varInfo : ctx.classIdMap.variables.fieldMap.values()) {
      writeField(out, varInfo);
    }
  }

  private void init(int idx) {
    out = outputs.get(idx);
    ctx.init(errors, this, idx, clazzes);
    ctx.constPool.addUtf8("SourceFile");
    ctx.constPool.addUtf8(ctx.clazz.sourceFile);
  }

  public void generate() {
    for (int i = 0; i < clazzes.size(); i++) {
      Clazz clazz = clazzes.get(i);
      init(i);
      ctx.classIdMap.variables.fieldMap.clear();
      ctx.classIdMap.variables.localMap.clear();
      header();
      supportPrint(clazz);
      ctx.memberProc.processFields();
      HasOutput methods = methods(new DynamicByteArray(), clazz);
      HasOutput constPool = constPool(new DynamicByteArray());
      out.write(constPool.getBytes());
      accessModifiers(clazz);
      classIndex();
      superClassIndex();
      interfaces(clazz);
      fields();
      out.write(methods.getBytes());
      attributes();
      out.flush();
    }
  }

  private void header() {
    out.write(0xCAFEBABE);
    out.write(JAVA_CLASS_MINOR_VERSION);
    out.write(JAVA_CLASS_MAJOR_VERSION);
  }

  private void interfaces(Clazz clazz) {
    out.write((short) clazz.interfaces.size());
    for (String interfaceName : clazz.interfaces) {
      // TODO: support interfaces from different packages
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_CLASS, (clazz.packageName + "." + interfaceName).replace('.', '/')));
    }
  }

  private HasOutput methods(HasOutput out, Clazz clazz) {
    ctx.opStack = new OperandStack();
    List<Method> methods = ctx.clazz.methods;
    if (!clazz.isInterface) {
      ctx.constPool.addUtf8("Code");
      ctx.memberProc.addClInit(methods);
    }
    out.write((short) methods.size());
    for (Method method : methods) {
      ctx.classIdMap.variables.localMap.clear();
      ctx.classIdMap.variables.localSize = 0;
      // keep idx 0 for "this" reference for super constructor call
      // TODO: is there a better way?
      if (Method.CONSTRUCTOR_NAME.equals(method.name.toString())) {
        ctx.classIdMap.variables.localSize = 1;
      }
      ctx.memberProc.processMethodArgs(method);
      ctx.memberProc.processLocalVars(method);
      ctx.methodGen.generate(out, method, clazz);
    }
    return out;
  }

  private void writeField(HasOutput out, VariableInfo varInfo) {
    short accFlags = 0;
    for (AccessFlag accFlag : varInfo.accFlags) {
      accFlags += accFlag.val;
    }
    out.write(accFlags);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, varInfo.name));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, TypeUtils.descr(varInfo)));
    // TODO: for some reason this still breaks for long and double
    if (varInfo.accFlags.contains(AccessFlag.ACC_FINAL)) {
      out.write((short) 1); // attribute size
      // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.2
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "ConstantValue"));
      out.write(2);
      out.write(
          ctx.constPool.indexOf(
              ctx.constPool.getTypeByte(varInfo.type), varInfo.val.getConstantValue(varInfo.type)));
    } else {
      out.write((short) 0); // attribute size
    }
  }

  private void superClassIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, "java/lang/Object"));
  }

  private void supportPrint(Clazz clazz) {
    // TODO: only add when needed
    if (!clazz.isInterface) {
      // hard-coded support for print
      ctx.constPool.addClass("java/lang/System");
      ctx.constPool.addClass("java/io/PrintStream");
      ctx.constPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    }
  }
}
