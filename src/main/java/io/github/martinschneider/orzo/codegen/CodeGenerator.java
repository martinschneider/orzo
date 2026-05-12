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
  private static final short JAVA_CLASS_MAJOR_VERSION = 52;
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
    int ownCount =
        ctx.classIdMap.variables.fieldMap.size()
            - ctx.classIdMap.variables.inheritedFieldNames.size();
    out.write((short) ownCount);
    for (VariableInfo varInfo : ctx.classIdMap.variables.fieldMap.values()) {
      if (!ctx.classIdMap.variables.inheritedFieldNames.contains(varInfo.name)) {
        writeField(out, varInfo);
      }
    }
  }

  private void init(int idx) {
    out = outputs.get(idx);
    ctx.init(errors, idx, clazzes);
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
      String ifaceFqn =
          interfaceName.contains(".") ? interfaceName : (clazz.packageName + "." + interfaceName);
      out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, ifaceFqn.replace('.', '/')));
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
      if (!method.accFlags.contains(AccessFlag.ACC_STATIC)) {
        // Slot 0 is reserved for 'this' in all non-static methods (including constructors)
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
    // Only add ConstantValue for primitive types and String (compile-time constants), not for
    // reference types (e.g. List<String> fields) whose initializers are not constant expressions.
    // TODO: for some reason this still breaks for long and double
    Object constVal = varInfo.val != null ? varInfo.val.getConstantValue(varInfo.type) : null;
    if (varInfo.accFlags.contains(AccessFlag.ACC_FINAL)
        && constVal != null
        && !varInfo.type.startsWith("L")
        && !varInfo.type.startsWith("[")) {
      out.write((short) 1); // attribute size
      // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.2
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "ConstantValue"));
      out.write(2);
      out.write(ctx.constPool.indexOf(ctx.constPool.getTypeByte(varInfo.type), constVal));
    } else {
      out.write((short) 0); // attribute size
    }
  }

  private void superClassIndex() {
    String superClass =
        ctx.clazz.baseClass != null ? ctx.clazz.baseClass.replace('.', '/') : "java/lang/Object";
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, superClass));
  }

  private void supportPrint(Clazz clazz) {
    // TODO: only add these constant-pool entries when the class actually uses System.out.println.
    // Unconditionally adding them bloats the constant pool of every generated class.
    // Fix: scan clazz.methods for MethodCall nodes with name "System.out.println" before adding.
    if (!clazz.isInterface) {
      ctx.constPool.addClass("java/lang/System");
      ctx.constPool.addClass("java/io/PrintStream");
      ctx.constPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    }
  }
}
